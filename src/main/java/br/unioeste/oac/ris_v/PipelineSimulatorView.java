package br.unioeste.oac.ris_v;

import java.util.List;

public class PipelineSimulatorView {
    private final static int COLUMNS = 32;

    public static void printPipelineState(int cycle, int pc, List<Integer> regs, List<Integer> memory){
        System.out.println("------------------------------------------------------------------------------------------------------------------------------\n");
        System.out.println("Ciclo de clock: " + cycle);

        System.out.println("PC: " + pc);

        System.out.println("\nRegistradores:");

        printMatrix(regs);

        System.out.println("\nMemória:");

        printMatrix(memory);
        System.out.println();
    }

    private static void printMatrix(List<Integer> data) {
        int rows = (data.size() + COLUMNS - 1) / COLUMNS;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                int index = i * COLUMNS + j;

                if (index < data.size()) {
                    System.out.printf("%-4d", data.get(index));
                } else {
                    System.out.printf("%-4s", "0");
                }
            }
            System.out.println();
        }
    }
}
