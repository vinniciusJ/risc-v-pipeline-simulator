package br.unioeste.oac.ris_v;

import java.util.List;

public class Main {
    public static void main(String[] args)  {
//        List<Instruction> instructions = List.of(
//                Instruction.addi(1, 0, 10),  // x1 = x0 + 10  (Inicializa x1 com 10)
//                Instruction.addi(2, 0, 20),  // x2 = x0 + 20  (Inicializa x2 com 20)
//                Instruction.or(3, 1, 2),     // x3 = x1 | x2  (Independente, usa valores definidos)
//                Instruction.and(4, 1, 2),    // x4 = x1 & x2  (Independente)
//                Instruction.add(5, 3, 4)    // x5 = x3 + x4  (Independente)
//        );

        List<Instruction> instructions = List.of(
                Instruction.addi(1, 0, 10), // r1 = 10
                Instruction.addi(2, 0, 20), // r2 = 20
                Instruction.sw(1, 0, 1)   // Mem[r1 + 0] = r1 (Mem[10] = 10)
//                Instruction.lw(3, 1, 4)     // r3 = Mem[r1 + 4] (r3 = 20)
        );

        PipelineSimulator.start(instructions);
    }
}
