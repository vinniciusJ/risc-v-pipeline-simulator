package br.unioeste.oac.ris_v;

import java.util.List;

public class Main {
    public static void main(String[] args)  {
        List<Instruction> instructions = List.of(
                Instruction.sw(15, 0, 5),  // Mem[r5 + 0] = r15 (Armazena r15 na memória) (Tipo-S)
                Instruction.lw(16, 0, 5), // r16 = Mem[r5 + 0] (Carrega o valor salvo de r15) (Tipo-I)
                Instruction.or(17, 16, 6), // r17 = r16 | r6 (Combina r16 e r6 com OR) (Tipo-R)
                Instruction.sub(18, 8, 9), // r18 = r8 - r9 (Tipo-R)
                Instruction.beq(17, 18, 12) // Se r17 == r18, salta para offset 12 (Tipo-B)
        );


        PipelineSimulator.start(instructions);
    }
}
