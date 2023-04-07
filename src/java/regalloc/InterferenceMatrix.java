package regalloc;

import gen.asm.Label;
import gen.asm.Register;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Set;


public class InterferenceMatrix {
    public int[][] adjMatrix;
    public int[] edgeMatrix;
    public int size;
    private HashMap<Register, Integer> indexHashMap;
    public Register[] regArrayCopy;

    public InterferenceMatrix(int pSize, Register[] registerArray){

        regArrayCopy = registerArray;
        indexHashMap = new HashMap<>();
        size = pSize;
        // register is from set so guarantee unique
        for(int i = 0; i<registerArray.length; i++){
            indexHashMap.put(registerArray[i], i);
        }
        adjMatrix = new int[size][size];
        edgeMatrix = new int[size];
        for(int i = 0; i< size;i++){
            edgeMatrix[i] = 0;
        }
    }

    public void AddEdge(Register r1, Register r2){
        try{
            int i = indexHashMap.get(r1);
            int j = indexHashMap.get(r2);
            adjMatrix[i][j] = 1;
            adjMatrix[j][i] = 1;
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public int getMaxEdge(Set<Integer> set){
        // k = t2-t9 + s0-s7
        int currentChoice = 0;
        for(int i = 1; i< set.size(); i++){
            if(set.contains(currentChoice)){
                currentChoice = i;
                break;
            }else{
                continue;
            }
        }
        int currentMax = -1;
        int k = 16; //16
        for(int i = 0; i<size; i++){
            if(!set.contains(i) && edgeMatrix[i] <= k && edgeMatrix[i] > currentMax){
                currentChoice = i;
                currentMax = edgeMatrix[i];
            }
        }
        if(currentMax == -1){
            return -1;
        }

        for(int i = 0; i<size; i++){
            // decrement the adjacency matrix;
            if(adjMatrix[currentChoice][i] == 1){
                edgeMatrix[i]--;
            }
        }
        return currentChoice;
    }

    public int Spill(Set<Integer> set){
        int currentChoice = 0;
        int currentMax = 0;
        for(int i = 0; i<size; i++){
            if(!set.contains(i)  && edgeMatrix[i] > currentMax){
                currentChoice = i;
                currentMax = edgeMatrix[i];
            }
        }

        for(int i = 0; i<size; i++){
            // decrement the adjacency matrix;
            if(adjMatrix[currentChoice][i] == 1){
                edgeMatrix[i]--;
            }
        }

    return currentChoice;

    }
}
