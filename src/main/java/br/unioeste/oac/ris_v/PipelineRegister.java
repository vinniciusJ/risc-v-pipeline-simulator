package br.unioeste.oac.ris_v;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class PipelineRegister {
    private Instruction instruction = null;
    private int pc;
    private int rs1;
    private int rs2;
    private int rd;
    private int imm;
    private int aluResult;
    private int lmd;
    private boolean branchTaken;
    private int a;
    private int b;
}