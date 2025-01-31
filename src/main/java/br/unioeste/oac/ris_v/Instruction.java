package br.unioeste.oac.ris_v;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class  Instruction {
    private int rd;
    private int rs1;
    private int rs2;
    private int imm;
    private InstructionType type;
    private int opcode;
    private Integer funct3;
    private Integer funct7;

    public static Instruction lw(int rd, int imm, int rs1) {
        return Instruction.builder()
                .rd(rd)
                .rs1(rs1)
                .imm(imm)
                .type(InstructionType.I)
                .opcode(0x03)
                .funct3(0x2)
                .build();
    }

    public static Instruction addi(int rd, int rs1, int imm) {
        return Instruction.builder()
                .rd(rd)
                .rs1(rs1)
                .imm(imm)
                .type(InstructionType.I)
                .opcode(0x13)
                .funct3(0x0)
                .build();
    }

    public static Instruction slli(int rd, int rs1, int imm) {
        return Instruction.builder()
                .rd(rd)
                .rs1(rs1)
                .imm(imm)
                .type(InstructionType.I)
                .opcode(0x13)
                .funct3(0x1)
                .funct7(0x00)
                .build();
    }


    public static Instruction sw(int rs2, int imm, int rs1) {
        return Instruction.builder()
                .rs1(rs1)
                .rs2(rs2)
                .imm(imm)
                .type(InstructionType.S)
                .opcode(0x23)
                .funct3(0x2)
                .build();
    }

    public static Instruction add(int rd, int rs1, int rs2) {
        return Instruction.builder()
                .rd(rd)
                .rs1(rs1)
                .rs2(rs2)
                .type(InstructionType.R)
                .opcode(0x33)
                .funct3(0x0)
                .funct7(0x00)
                .build();
    }

    public static Instruction sub(int rd, int rs1, int rs2) {
        return Instruction.builder()
                .rd(rd)
                .rs1(rs1)
                .rs2(rs2)
                .type(InstructionType.R)
                .opcode(0x33)
                .funct3(0x0)
                .funct7(0x20)
                .build();
    }

    public static Instruction or(int rd, int rs1, int rs2) {
        return Instruction.builder()
                .rd(rd)
                .rs1(rs1)
                .rs2(rs2)
                .type(InstructionType.R)
                .opcode(0x33)
                .funct3(0x6)
                .funct7(0x00)
                .build();
    }

    public static Instruction and(int rd, int rs1, int rs2) {
        return Instruction.builder()
                .rd(rd)
                .rs1(rs1)
                .rs2(rs2)
                .type(InstructionType.R)
                .opcode(0x33)
                .funct3(0x7)
                .funct7(0x00)
                .build();
    }

    public static Instruction beq(int rs1, int rs2, int imm) {
        return Instruction.builder()
                .rs1(rs1)
                .rs2(rs2)
                .imm(imm)
                .type(InstructionType.B)
                .opcode(0x63)
                .funct3(0x0)
                .build();
    }

    public static Instruction bne(int rs1, int rs2, int imm) {
        return Instruction.builder()
                .rs1(rs1)
                .rs2(rs2)
                .imm(imm)
                .type(InstructionType.B)
                .opcode(0x63)
                .funct3(0x1)
                .build();
    }
}