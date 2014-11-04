package com.dianping.algorithm.dp_review_mining.nlp.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;



public class Review_Filter_Few_Words {

    /**
     * @param args
     */
    private int hanziCntThreathold;
    private int wordCntThreathold;
    private double perHanziThreathold;
    private Count cnt= new Count();
    
    class Count{
        int hanziCnt;
        int disHanziCnt;
        int wordsCnt;
        int disWordsCnt;
        double perHanzi;
        Count()
        {
            hanziCnt=0;
            disHanziCnt=0;
            wordsCnt=0;
            disWordsCnt=0;
            perHanzi = 0;
        }
    }
    
    public Review_Filter_Few_Words(int hanziCntThreathold, int wordCntThreathold, double perHanziThreathold){
        this.hanziCntThreathold=hanziCntThreathold;
        this.wordCntThreathold=wordCntThreathold;
        this.perHanziThreathold = perHanziThreathold;
    }
    
    
    public boolean doFilter(String review){
        cntHanzi(review, cnt);
        cntWords(review, cnt);
        
        if (cnt.disHanziCnt>=hanziCntThreathold || cnt.disWordsCnt >=wordCntThreathold||cnt.perHanzi > perHanziThreathold)
            return false;
        return true;
        

    }
    
   
    
private void cntWords(String review, Count cnt) {
      
       cnt.wordsCnt=0;
       cnt.disWordsCnt=0;
       HashSet<String> wordSet = new HashSet<String>();
       for (String s:review.split(" +|\\.|,|;|\\(|\\)|'")){
           cnt.wordsCnt++;
           if (s.length()>3)
               wordSet.add(s);  
       }
       cnt.disWordsCnt=wordSet.size();
    }


private void cntHanzi(String review, Count cnt){
       cnt.hanziCnt=0;
       cnt.disHanziCnt=0;
       char[] charArray  = review.toCharArray(); 
       Set<Character> hanziSet = new HashSet<Character>();
       for (int i = 0; i < charArray.length; i++) {  
            if ((charArray[i] >= 0x4e00)&&(charArray[i] <= 0x9fbb)){  
                cnt.hanziCnt++;
               hanziSet.add(charArray[i]);
             }       
       }
       cnt.disHanziCnt = hanziSet.size();
       cnt.perHanzi = cnt.hanziCnt/(double)charArray.length;
//       if (hanziCnt>0 && disHanziCnt<hanziCntThreathold)//有汉字并且独立汉字数少
//           return true;
//       return false;
   }
    
    
    public static void main1(String[] args) throws IOException {
       
        Review_Filter_Few_Words ff= new Review_Filter_Few_Words(15,15,0.7);
        BufferedReader br= new BufferedReader(new FileReader("test/testReview.csv"));
        BufferedWriter bw= new BufferedWriter(new FileWriter("test/testReviewResult.csv"));
        String temp;
        while((temp = br.readLine()) != null)
        {
            try
            {
            bw.write(temp.split(",",2)[0]+","+temp.split(",",2)[1].replace(',', '.')+"," +ff.doFilter(temp.split(",",2)[1]));
            }
            catch(Exception e)
            {
                System.out.println(temp);
                continue;
            }
            bw.write('\n');
        }
        br.close();
        bw.close();
        
    }
    
    
    

}
