package br.unioeste.oac.ris_v;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipelineSimulator {
    private static final int LW = 0x03;
    private static final int SW = 0x23;
    private static final int BEQ = 0x63;
    private static final int ARITH_OP = 0x33;
    private static final int ADDI = 0x13;
    private static final int REG_COUNT = 32;
    private static final int MEM_SIZE = 1024;

    private PipelineRegister IF_ID = new PipelineRegister();
    private PipelineRegister ID_EX = new PipelineRegister();
    private PipelineRegister EX_MEM = new PipelineRegister();
    private PipelineRegister MEM_WB = new PipelineRegister();

    private int pc = 0;
    private int cycle = 0;
    private boolean stall = false;
    private boolean branchResolved = false;

    private final List<Integer> regs = new ArrayList<>(Collections.nCopies(REG_COUNT, 0));
    private final List<Integer> memory = new ArrayList<>(Collections.nCopies(MEM_SIZE, 0));
    private final List<Instruction> instructions;

    public PipelineSimulator(List<Instruction> instructions) {
        this.instructions = new ArrayList<>(instructions);
        regs.set(0, 0);
    }

    public void run() {
        while (cycle < instructions.size() + 4 && cycle < 100) {
            writeBack();
            memoryAccess();
            execute();

            if (!stall) {
                decode();
            } else {
                stall = false;
            }

            instructionFetch();

            updatePipelineRegisters();

            cycle++;

            PipelineSimulatorView.printPipelineState(cycle, pc, regs, memory);
        }
    }

    private void instructionFetch() {
        if (pc >= instructions.size() || branchResolved) return;

        IF_ID.setInstruction(instructions.get(pc));
        IF_ID.setPc(pc);

        pc++;
    }

    private void decode() {
        if (IF_ID.getInstruction() == null) {
            ID_EX.setInstruction(null);

            return;
        }

        Instruction instruction = IF_ID.getInstruction();
        int opcode = instruction.getOpcode();

        ID_EX.setInstruction(instruction);
        ID_EX.setPc(IF_ID.getPc());

        ID_EX.setRs1(instruction.getRs1());
        ID_EX.setRs2(instruction.getRs2());
        ID_EX.setRd(instruction.getRd());
        ID_EX.setImm(instruction.getImm());

        if (checkDataHazard(ID_EX.getRs1()) || checkDataHazard(ID_EX.getRs2())) {
            stall = true;
            ID_EX.setInstruction(null);

            return;
        }

        ID_EX.setA(getForwardedValue(ID_EX.getRs1()));
        ID_EX.setB(getForwardedValue(ID_EX.getRs2()));

        switch (opcode) {
            case LW, ADDI, SW, BEQ -> ID_EX.setImm(instruction.getImm());
        }
    }

    private boolean checkDataHazard(int register) {
        return register != 0 &&
                ((EX_MEM.getRd() == register && EX_MEM.getInstruction() != null && EX_MEM.getInstruction().getOpcode() == LW) ||
                        (MEM_WB.getRd() == register && MEM_WB.getInstruction() != null && MEM_WB.getInstruction().getOpcode() == LW));
    }


    private int getForwardedValue(int register) {
        if (register == 0) {
            return 0;
        }

        if (EX_MEM.getRd() == register && EX_MEM.getInstruction() != null) {
            return EX_MEM.getAluResult();
        } else if (MEM_WB.getRd() == register && MEM_WB.getInstruction() != null) {
            return MEM_WB.getInstruction().getOpcode() == LW ? MEM_WB.getLmd() : MEM_WB.getAluResult();
        }

        return regs.get(register);
    }

    private void execute() {
        if (ID_EX.getInstruction() == null) {
            EX_MEM.setInstruction(null);
            return;
        }

        Instruction instruction = ID_EX.getInstruction();

        int opcode = instruction.getOpcode();

        EX_MEM.setInstruction(instruction);
        EX_MEM.setPc(ID_EX.getPc());
        EX_MEM.setRd(ID_EX.getRd());
        EX_MEM.setRs2(ID_EX.getRs2());

        EX_MEM.setB(ID_EX.getB());

        switch (opcode) {
            case LW, SW, ADDI -> EX_MEM.setAluResult(ID_EX.getA() + ID_EX.getImm());
            case BEQ -> {
                EX_MEM.setBranchTaken(ID_EX.getA() == ID_EX.getB());

                if (EX_MEM.isBranchTaken()) {
                    pc = ID_EX.getPc() + ID_EX.getImm();
                    branchResolved = true;
                }
            }
            case ARITH_OP -> {
                int funct3 = instruction.getFunct3();

                switch (funct3) {
                    case 0x0 -> EX_MEM.setAluResult(instruction.getFunct7() == 0 ? ID_EX.getA() + ID_EX.getB() : ID_EX.getA() - ID_EX.getB());
                    case 0x6 -> EX_MEM.setAluResult(ID_EX.getA() | ID_EX.getB());
                    case 0x7 -> EX_MEM.setAluResult(ID_EX.getA() & ID_EX.getB());
                }
            }
        }
    }

    private void memoryAccess() {
        if (EX_MEM.getInstruction() == null) {
            MEM_WB.setInstruction(null);

            return;
        }

        Instruction instruction = EX_MEM.getInstruction();
        int opcode = instruction.getOpcode();

        MEM_WB.setInstruction(instruction);
        MEM_WB.setRd(EX_MEM.getRd());
        MEM_WB.setAluResult(EX_MEM.getAluResult());

        switch (opcode) {
            case LW -> MEM_WB.setLmd(memory.get(EX_MEM.getAluResult() / 4));
            case SW -> memory.set(EX_MEM.getAluResult(), EX_MEM.getB());
        }
    }

    private void writeBack() {
        if (MEM_WB.getInstruction() == null) {
            return;
        }

        int opcode = MEM_WB.getInstruction().getOpcode();

        if (opcode == LW) {
            regs.set(MEM_WB.getRd(), MEM_WB.getLmd());
        } else if (opcode != SW && opcode != BEQ) {
            regs.set(MEM_WB.getRd(), MEM_WB.getAluResult());
        }
    }

    private void updatePipelineRegisters() {
        if (branchResolved) {
            IF_ID = new PipelineRegister();
            ID_EX = new PipelineRegister();
            branchResolved = false;
        }
    }

    public static void start(List<Instruction> instructions) {
        new PipelineSimulator(instructions).run();
    }
}