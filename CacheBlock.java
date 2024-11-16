public class CacheBlock{
  
  private int words[];
  private int order;
  private int valid;
  
  public CacheBlock(){
    words = new int[2]; 
    words[0] = 0;
    words[1] = 0;
    order = 0;
    valid = 0;
  }
  
  public CacheBlock(int w0, int w1, int order, int valid){
    words = new int[2];
    words[0] = w0;
    words[1] = w1;
    this.order = order;
    this.valid = valid;
  }
  
  public void setBlock(CacheBlock cb){
    words[0] = cb.getWord(0);
    words[1] = cb.getWord(1);
    order = cb.getOrder();
    valid = cb.getValid();
  }
  
  public int getWord(int idx){
    return words[idx];
  }
  
  public void setWord(int idx, int memoryAddress){
    words[idx] = memoryAddress;
  }
  
  public int getOrder(){
     return order; 
  }
  
  public void setOrder(int value){
     order = value; 
  }
  
  public void decrementOrder(){
     order--; 
  }
  
  public int getValid(){
     return valid; 
  }
  
  public void setValid(int value){
     valid = value;
  }
  
}