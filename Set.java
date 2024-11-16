import java.util.ArrayList;

public class Set{
  
  ArrayList<CacheBlock> blocks;
  int capacity;
  
  public Set(int blocksPerSet){
    blocks = new ArrayList<>(blocksPerSet);
    capacity = 0;
  }
  
  public void add(CacheBlock block){
     blocks.add(block);
     capacity++;
  }
  
  public CacheBlock get(int i){
     return blocks.get(i); 
  }
  
  public void set(int i, CacheBlock block){
 //    blocks.setBlock(block);
       blocks.set(i, block);
  }
  
  public int getCapacity(){
     return capacity; 
  }
}