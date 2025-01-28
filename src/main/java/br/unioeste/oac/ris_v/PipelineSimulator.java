package br.unioeste.oac.ris_v;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipelineSimulator {

    private static final int NOP = -1;
    private static final int LW = 0x03;
    private static final int SW = 0x23;
    private static final int BEQ = 0x63;
    private static final int REG_COUNT = 32;

    private int cycle = 0;
    private int pc = 0;

    private final List<Integer> regs = new ArrayList<>(Collections.nCopies(REG_COUNT, 1));
    private final List<Integer> memory = new ArrayList<>(Collections.nCopies(1024, 0));

    private final List<Integer> instructions;

    private final List<Integer> IFID, IDEX, EXMEM, MEMWB;

    private int EXMEM_ALUOut, EXMEM_B;
    private int MEMWB_ALUOut, MEMWB_LMD;
    private int IDEX_A, IDEX_B;

    public PipelineSimulator(List<Instruction> instructions) {
        this.instructions = instructions.stream().map(Instruction::toBinary).toList();

        this.IFID = new ArrayList<>(Collections.nCopies(instructions.size(), NOP));
        this.IDEX = new ArrayList<>(Collections.nCopies(instructions.size(), NOP));
        this.EXMEM = new ArrayList<>(Collections.nCopies(instructions.size(), NOP));
        this.MEMWB = new ArrayList<>(Collections.nCopies(instructions.size(), NOP));
    }

    public void run() {
        while (cycle < instructions.size() + 4) {

            for (int i = 0; i < instructions.size(); i++) {
                if (MEMWB.get(i) != NOP) {
                    writeBack(MEMWB.get(i));
                }
            }

            for (int i = 0; i < instructions.size(); i++) {
                if (EXMEM.get(i) != NOP) {
                    memoryAccess(i, EXMEM.get(i));
                }
            }

            for (int i = 0; i < instructions.size(); i++) {
                if (IDEX.get(i) != NOP) {
                    execute(i, IDEX.get(i));
                }
            }

            for (int i = 0; i < instructions.size(); i++) {
                if (IFID.get(i) != NOP) {
                    decode(i, IFID.get(i));
                }
            }

            for (int i = instructions.size() - 1; i >= 0; i--) {
                if (cycle >= i + 4) {
                    fetch(i);
                }
            }

            for (int i = 0; i < instructions.size(); i++) {
                MEMWB.set(i, EXMEM.get(i));
                EXMEM.set(i, IDEX.get(i));
                IDEX.set(i, IFID.get(i));
                IFID.set(i, instructions.get(i));
            }

            cycle++;

            PipelineSimulatorView.printPipelineState(cycle, pc, regs, memory);
        }
    }

    private void fetch(int index) {
        if (cycle >= index + 4) {
            IFID.set(index, instructions.get(index));
        }
    }

    private void decode(int index, int instruction) {
        IDEX.set(index, instruction);

        int rs1 = (instruction >> 15) & 0x1F;
        int rs2 = (instruction >> 20) & 0x1F;

        IDEX_A = regs.get(rs1);
        IDEX_B = regs.get(rs2);
    }

    private void execute(int index, int instruction) {
        EXMEM.set(index, instruction);

        int opcode = instruction & 0x7F;

        if (opcode == LW || opcode == SW) {
            int imm = (instruction >> 20);
            EXMEM_ALUOut = IDEX_A + imm;
        } else if (opcode == BEQ) {
            if (IDEX_A == IDEX_B) {
                pc = IDEX_A + ((instruction >> 7) & 0x1F);
            }
        } else if (opcode == 0x33) {
            int funct3 = (instruction >> 12) & 0x7;
            int funct7 = (instruction >> 25) & 0x7F;

            if (funct3 == 0x0) {
                if (funct7 == 0x00) {
                    EXMEM_ALUOut = IDEX_A + IDEX_B;
                } else if (funct7 == 0x20) {
                    EXMEM_ALUOut = IDEX_A - IDEX_B;
                }
            } else if (funct3 == 0x7) {
                EXMEM_ALUOut = IDEX_A & IDEX_B;
            } else if (funct3 == 0x6) {
                EXMEM_ALUOut = IDEX_A | IDEX_B;
            }
        } else if (opcode == 0x13) {
            int imm = (instruction >> 20);
            EXMEM_ALUOut = IDEX_A + imm;
        }

        EXMEM_B = IDEX_B;
    }

    private void memoryAccess(int index, int instruction) {
        MEMWB.set(index, instruction);

        int opcode = instruction & 0x7F;

        if (opcode == LW) {
            MEMWB_LMD = memory.get(EXMEM_ALUOut / 4);
        } else if (opcode == SW) {
            memory.set(EXMEM_ALUOut / 4, EXMEM_B);
        }

        MEMWB_ALUOut = EXMEM_ALUOut;
    }

    private void writeBack( int instruction) {
        int opcode = instruction & 0x7F;
        int rd = (instruction >> 7) & 0x1F;

        if (opcode == LW) {
            regs.set(rd, MEMWB_LMD);
        } else {
            regs.set(rd, MEMWB_ALUOut);
        }
    }

    public static void start(List<Instruction> instructions){
        new PipelineSimulator(instructions).run();
    }
}
