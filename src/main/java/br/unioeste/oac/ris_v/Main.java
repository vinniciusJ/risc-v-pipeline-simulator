package br.unioeste.oac.ris_v;

import java.util.List;

public class Main {
    public static void main(String[] args)  {
        List<Instruction> instructions = List.of(
                Instruction.add(9, 1, 2)
        );

        PipelineSimulator.start(instructions);
    }
}
