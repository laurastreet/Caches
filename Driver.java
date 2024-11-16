//Laura Street G00097274
//Perri Katcher G01114713

import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Driver{
             
    public static void main(String[] args) throws IOException{
        int NBC = Integer.parseInt(args[0]);
        int BSZ = Integer.parseInt(args[1]);
        int MSZ = Integer.parseInt(args[2]);
        int n = Integer.parseInt(args[3]);
        String evictionPolicy = args[4];
        int HT = Integer.parseInt(args[5]);
        int MAC = Integer.parseInt(args[6]);
        String filename = args[7];
    
        ArrayList<Integer> memoryAddresses = new ArrayList<>(); //to store memoryAddresses
        Scanner scan = new Scanner(new File(filename));
        // int idx  = 0;
        while(scan.hasNext()){
            memoryAddresses.add(scan.nextInt()); //add int to list
            // System.out.print("memoryAddress: " + memoryAddresses.get(idx));
            // idx++;
        }
    
        int sets; //number of sets in each type of cache
        int blocksPerSet;
        
        //direct-mapped cache - NBC sets, each containing 1 cacheBlock
        if(n==1){
            blocksPerSet = 1; //1 cacheBlock per set
            sets = NBC; //# of sets = NBC
 
            //initialize cache
            ArrayList<Set> dmCache = new ArrayList<>(sets); //create a cache with sets # of sets
            CacheBlock block;
            Set dmSet;
            for(int i=0; i<sets; i++){
                block = new CacheBlock(); //create a new cacheBlock
                dmSet = new Set(blocksPerSet); //create a new Set with 1 cacheBlock per Set
                dmSet.add(block); //add cacheBlock to set
                dmCache.add(dmSet); //add set to dmCache
            }//now we should have a cache with sets number of sets, each containing one block
      
            //search cache for memoryAddresses read in from file
            int hit;
            int totalCacheHits=0;
            for(int j=0; j<memoryAddresses.size(); j++){
                hit = searchdmCache(memoryAddresses.get(j), dmCache, sets); 
  //       System.out.print("\nIteration: " + i + " hit?: " + hit);
                totalCacheHits += hit;
            }//now we have searched the cache for all memoryAddresses
      
   //         System.out.print("\ntotalHits: " + totalCacheHits);
            //printResults
            printResults(memoryAddresses.size(), totalCacheHits, MAC, BSZ, HT);
        }//end dm-cache
    
        //fully-associative cache - 1 large set containing n cacheBlocks
        else if(n==NBC){
            blocksPerSet = NBC;
            CacheBlock block;
            Set faSet = new Set(blocksPerSet); //creates an arrayList of cacheBlocks (a set) 
            //initialize the cache
            for(int i=0; i<blocksPerSet; i++){
                block = new CacheBlock(); 
                faSet.add(block); //add new cacheBlock to the 1 large set
            }
 //     System.out.print("fa set initialized\n");
            ArrayList<Set> faCache = new ArrayList<>(1); //1 large set
            faCache.add(faSet); //add the 1 large set to the new cache
            //now we should have a cache with NBC sets, each containing one block
      
            //search the cache
            int hit;
            int totalCacheHits = 0;
            for(int j=0; j<memoryAddresses.size(); j++){
                hit = searchfaCache(memoryAddresses.get(j), faCache, NBC, evictionPolicy); 
                totalCacheHits += hit;
            }//now we have searched the cache for all memoryAddresses
            printResults(memoryAddresses.size(), totalCacheHits, MAC, BSZ, HT);
        }//end fa-cache
    
        else{ //n-wayassociative cache
            blocksPerSet = n; //each set contains n cacheBlocks
            sets = NBC/n;
            CacheBlock block;
            Set nwSet;
            ArrayList<Set> nwCache = new ArrayList<>(sets); //ArrayList containing sets # of sets
            for(int i=0; i<sets; i++){
                nwSet = new Set(blocksPerSet); //create a new Set
                for(int j=0; j<n; j++){ //add n cacheBlocks to each set
                    block = new CacheBlock();
                    nwSet.add(block); //add cacheBlock to set
                }
                nwCache.add(nwSet); //add newly created set to nwCache
            }//now we should have a cache with sets # of sets, each containing n blocks
            
            //search nwayCache
            int hit;
            int totalCacheHits = 0;
            for(int k=0; k<memoryAddresses.size(); k++){
                hit = searchnwCache(memoryAddresses.get(k), nwCache, NBC, n, evictionPolicy);
                totalCacheHits += hit;
            }
            printResults(memoryAddresses.size(), totalCacheHits, MAC, BSZ, HT);
        }//end nwayCache        
    }//end main
  
    //there will only be one possible place the memoryAddress can be
    static int searchdmCache(int memoryAddress, ArrayList<Set> dmCache, int sets){
        int cacheHit;
        int tag;
        if(memoryAddress%2==0) //even memoryAddress
            tag = memoryAddress;
        else
            tag = memoryAddress -1;
        int set = (memoryAddress/2)%sets; //tells us where to look for memoryAddress (which set)
        Set targetSet = dmCache.get(set);
        CacheBlock targetBlock = targetSet.get(0); //each set only contains one block
  //      System.out.print("\ntag: " + tag + ", set: " + set + ", word0: " + targetBlock.getWord(0));
        if(tag==targetBlock.getWord(0)) //compare tag with word0
            cacheHit = 1; //cacheHit
        else{ //otherwise update both words in the block
            if(memoryAddress%2==0){
                targetBlock.setWord(0, memoryAddress);
                targetBlock.setWord(1, memoryAddress+1);
            }
            else{
                targetBlock.setWord(0, memoryAddress-1);
                targetBlock.setWord(1, memoryAddress);
            }
            cacheHit = 0; //cacheMiss
        }
    //    printdmCache(dmCache, sets, cacheHit);
        return cacheHit;
    }//end searchdmCache
  
    static int searchfaCache(int memoryAddress, ArrayList<Set> faCache, int NBC, 
                           String evictionPolicy){
        int tag;
        int cacheHit =0; //cacheHit will be set = 1 for cacheHit, 0 for cacheMiss
        if(memoryAddress%2==0)//even memoryAddress 
            tag = memoryAddress;
        else
            tag = memoryAddress-1;
        Set bigSet = faCache.get(0); //there is only one set
        CacheBlock block;
        boolean freeBlock = false; //initially, we have no block assigned as freeBlock
        int freeBlockIdx = 0; //initially, freeBlockIdx = 0
      
        //have to search the whole cache for our tag representing memoryAddress
        //we will also use valid bit to keep track of a free cacheBlock (if any)
    //    System.out.print("\ntag: " + tag);
        for(int i=0; i<NBC; i++){ //have to search the whole cache
            block = bigSet.get(i); //return cacheBlock at idx i
        
            //check for a freeBlock in case we need one later if cacheMiss
            if(freeBlock==false&&block.getValid()==0){ 
                freeBlockIdx = i; //keep track of freeBlock's idx in cache
                freeBlock = true; //indicate that we have a free block
            }
        
            //find the block with the highest order 
            int highestOrder = 0; //will use for cacheHit + LRU
            CacheBlock cb;
            for(int k=0;k<NBC; k++){
                cb = bigSet.get(k);
                if(cb.getOrder()>highestOrder)
                    highestOrder = cb.getOrder(); 
            }
     //       System.out.print("\nhighestOrder: " + highestOrder);
        
            //search cache for block (only have to compare word0 with tag
            if(tag==block.getWord(0)){ 
                cacheHit = 1;
                //check to see if we have LRU evictionPolicy
                if(evictionPolicy.equals("LRU")){
                    //if LRU, we have to update the orders       
                    int p = block.getOrder(); //get the previous order
       //             System.out.print("\nprevious order: " + p);
                    block.setOrder(highestOrder); //set the order of the block to the highest possible order
                    CacheBlock c;
                    for(int j=0; j<NBC; j++){ //decrement orders for all blocks with orders
                      //higher than p
                        c = bigSet.get(j);
                        if(c.getOrder()>p&&j!=i){
                            c.decrementOrder();
                        }
                    }
                }//end updating blocks for LRU replacement policy
                break; //can stop iterating once we have a cacheHit
            }//end cacheHit    
            else
                cacheHit = 0;
        }//end searchCacheforBlock
        
        if(cacheHit==0){ //if we have a cacheMiss
            //first, we set up the new CacheBlock
            int words0 = 0, words1 = 0;
            if(memoryAddress%2==0){
                words0 = memoryAddress;
                words1 = memoryAddress+1;
            }
            if(memoryAddress%2==1){
                words0 = memoryAddress-1;
                words1 = memoryAddress;
            }
          
            //count number of valid blocks in cache
            int numBlocks = 0;
            CacheBlock d;
            for(int y=0; y<NBC; y++){
                d = bigSet.get(y);
                if(d.getValid()==1)
                    numBlocks++;
            }
            //order will be the highest value (regardless of LRU or FIFO)
            CacheBlock memAddrBlockSpace = new CacheBlock(words0, words1, numBlocks, 1);
        
            //if we have space in the cache
            if(cacheHit==0&&freeBlock==true){ 
                bigSet.set(freeBlockIdx, memAddrBlockSpace); 
            }  
            else{ //if no space, find the oldest block and replace it (find block with order =0)
                int lowestOrder = 0;
                int idxLowestOrder = 0;
                CacheBlock cb;
                for(int i=0;i<NBC;i++){
                    cb = bigSet.get(i); //get cacheBlock at index i
                    if(cb.getOrder()==0){
                        idxLowestOrder = i; //set the idx of the cacheBlock with highest order to i
                    }
                }
                //order will be the highest value (regardless of LRU or FIFO)
                CacheBlock memAddrBlockNoSpace = new CacheBlock(words0, words1, NBC-1, 1);
                bigSet.set(idxLowestOrder, memAddrBlockNoSpace); //evict the highestOrder block and replace
                //with new cacheBlock
                //decrement all other orders -1
                for(int i=0; i<NBC; i++){
                    if(i!=idxLowestOrder){ //do not change order for new block  
                        cb = bigSet.get(i);  
                        cb.decrementOrder();
                    }
                }
            }//end cacheMiss, no space
        }//end cacheMiss
   //     printfaCache(faCache, NBC, cacheHit);
        return cacheHit;
    }//end searchfaCache

    
    static int searchnwCache(int memoryAddress, ArrayList<Set> nwCache, int NBC, int n, 
                             String evictionPolicy){
        int tag;
        int cacheHit =0; //cacheHit will be set = 1 for cacheHit, 0 for cacheMiss
        if(memoryAddress%2==0)//even memoryAddress 
            tag = memoryAddress;
        else
            tag = memoryAddress-1;
        int numSets = NBC/n; //number of sets in cache
        int set = (memoryAddress/2)% numSets; //set where we will look for memoryAddress
        
        Set targetSet = nwCache.get(set); //get the set where we will look for memoryAddress
        CacheBlock block;
        boolean freeBlock = false; //initially, we have no block assigned as freeBlock
        int freeBlockIdx = 0; //initially, freeBlockIdx = 0
      
        //have to search all blocks in the set
        //we will also use valid bit to keep track of a free cacheBlock (if any)
   //     System.out.print("\ntag: " + tag + ", set: " + set);
    //    System.out.print("\nnumSets: " + numSets);
        for(int i=0; i<n; i++){ 
            block = targetSet.get(i); //return cacheBlock at idx i
        
            //check for a freeBlock in case we need one later if cacheMiss
            if(freeBlock==false&&block.getValid()==0){ 
                freeBlockIdx = i; //keep track of freeBlock's idx in cache
                freeBlock = true; //indicate that we have a free block
            }
        
            //find the block with the highest order 
            int highestOrder = 0; //will use for cacheHit + LRU
            CacheBlock cb;
            for(int j=0;j<n; j++){
                cb = targetSet.get(j);
                if(cb.getOrder()>highestOrder)
                    highestOrder = cb.getOrder(); 
            }
       //     System.out.print("\nhighestOrder: \n" + highestOrder);
        
            //search cache for block (only have to compare word0 with tag
            if(tag==block.getWord(0)){ 
                cacheHit = 1;
                //check to see if we have LRU evictionPolicy
                if(evictionPolicy.equals("LRU")){
                    //if LRU, we have to update the orders       
                    int p = block.getOrder(); //get the previous order
       //             System.out.print("\nprevious order: \n" + p);
                    block.setOrder(highestOrder); //set the order of the block to the highest possible order
                    CacheBlock c;
                    for(int k=0; k<n; k++){ //decrement orders for all blocks with orders
                      //higher than p
                        c = targetSet.get(k);
                        if(c.getOrder()>p&&k!=i){
                            c.decrementOrder();
                        }
                    }
                }//end updating blocks for LRU replacement policy
                break; //can stop iterating once we have a cacheHit
            }//end cacheHit    
            else
                cacheHit = 0;
        }//end searchCacheforBlock
        
        if(cacheHit==0){ //if we have a cacheMiss
            //first, we set up the new CacheBlock
            int words0 = 0, words1 = 0;
            if(memoryAddress%2==0){
                words0 = memoryAddress;
                words1 = memoryAddress+1;
            }
            if(memoryAddress%2==1){
                words0 = memoryAddress-1;
                words1 = memoryAddress;
            }
          
            //count number of valid blocks in set
            int numBlocks = 0;
            CacheBlock d;
            for(int y=0; y<n; y++){
                d = targetSet.get(y);
                if(d.getValid()==1)
                    numBlocks++;
            }
            //order will be the highest value (regardless of LRU or FIFO)
            CacheBlock memAddrBlockSpace = new CacheBlock(words0, words1, numBlocks, 1);
        
            //if we have space in the cache
            if(cacheHit==0&&freeBlock==true){ 
                targetSet.set(freeBlockIdx, memAddrBlockSpace); 
            }  
            else{ //if no space, find the oldest block in the set and replace it (find block with order =0)
                int lowestOrder = 0;
                int idxLowestOrder = 0;
                CacheBlock cb;
                for(int i=0;i<n;i++){
                    cb = targetSet.get(i); //get cacheBlock at index i
                    if(cb.getOrder()==0){
                        idxLowestOrder = i; //set the idx of the cacheBlock with highest order to i
                    }
                }
                //order will be the highest value (regardless of LRU or FIFO)
                CacheBlock memAddrBlockNoSpace = new CacheBlock(words0, words1, n-1, 1);
                targetSet.set(idxLowestOrder, memAddrBlockNoSpace); //evict the highestOrder block and replace
                //with new cacheBlock
                //decrement all other orders -1
                for(int l=0; l<n; l++){
                    if(l!=idxLowestOrder){ //do not change order for new block  
                        cb = targetSet.get(l);  
                        cb.decrementOrder();
                    }
                }
            }//end cacheMiss, no space
        }//end cacheMiss
    //    printnwCache(nwCache, NBC, n, cacheHit);
        return cacheHit;     
    }//end searchnwCache
  
    static void printdmCache(ArrayList<Set> cache, int NBC, int cacheHit){
        Set bigSet;
        CacheBlock cb;
        System.out.print("\ncacheHit: " + cacheHit);
        for(int i=0; i<NBC; i++){
            bigSet = cache.get(i); //return the set at index i
            cb = bigSet.get(0); //return the cacheBlock (only 1 per set)
            System.out.print("\nset: " + i + ", block0, word0: " + cb.getWord(0) + " word1: " 
                     + cb.getWord(1)); 
        }
        System.out.print("\n--------------------------------\n");
    }
  
    static void printfaCache(ArrayList<Set> cache, int NBC, int cacheHit){
        Set bigSet = cache.get(0); //get the one big set from the cache
        CacheBlock cb;
        System.out.print("\ncacheHit: " + cacheHit);
        for(int i=0; i<NBC; i++){
            cb = bigSet.get(i);
            System.out.print("\nset: " + i + ", block0: ,word0, " + cb.getWord(0) + " word1: "
                      + cb.getWord(1) + ", order: " + cb.getOrder());
        }
        System.out.print("\n-------------------------------\n");
    }
  
    static void printnwCache(ArrayList<Set> nwCache, int NBC, int n, int cacheHit){
        Set s;
        CacheBlock cb;
        int numSets = NBC/n;
        for(int i=0; i<numSets; i++){
             s = nwCache.get(i); //get the set at index i of nwCache
             for(int j=0; j<n; j++){ //print each block
                 cb = s.get(j); //get cacheBlock at index j
                 System.out.print("block: " + j + " word0: " + cb.getWord(0) + "\t");
             }
             System.out.print("\n");
        }
        System.out.print("\n-------------------------------\n");
    }
   
    static void printResults(int length, int totalCacheHits, int MAC, int BSZ, int HT){
        int numMisses = length-totalCacheHits; //equals number of misses
        double missRatio = (double) numMisses/length;
        int missPenalty = numMisses*MAC*BSZ;
        int totalCycles = missPenalty + totalCacheHits*HT;
        System.out.print("\nResults: totalMemoryReferences: " + numMisses + ", missRatio: " 
                       + missRatio + ", Penalty: " + missPenalty + ", totalCycles: " 
                       + totalCycles);
    }
}