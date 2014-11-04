package com.dianping.algorithm.dp_review_mining.nlp.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Review_Filter_Ads{

    /**
     * @param args
     */
    private Set<String> AdWords;
    private Set<String> AdRegs;
    public Review_Filter_Ads(Set<String> AdWords, Set<String> AdRegs)
    {
        if (AdWords.size()==0 && AdRegs.size()==0)
        {
            throw new RuntimeException("filter words and regs are empty!");
        }
        this.AdWords=AdWords;
        this.AdRegs=AdRegs;
    }
    
    public boolean doFilter(String review){
        if (!review.toLowerCase().contains("dianping"))
            return filterWords(review) || filterRegs(review);
        else 
            return false;
                 
    }
    
    private boolean filterRegs(String review) {

        for(String s:AdRegs){
           Pattern pattern= Pattern.compile(s,Pattern.DOTALL);
           Matcher matcher = pattern.matcher(review); 
           if (matcher.matches())
               return true;
        }
        return false;
       
    }

    private boolean filterWords(String review) {
        for(String s:AdWords){
            if (review.toLowerCase().contains(s.toLowerCase()))
                return true;
        }
        return false;
    }

    
    
    
     public static void main(String[] args) throws IOException, SQLException {
         HashSet<String> words= new HashSet<String>();
         HashSet<String> regs= new HashSet<String>();
         words.add("http:");
         words.add("www.");
         words.add(".com");
         words.add(".org");
         words.add(".net");
         words.add(".cn");
         words.add(".taobao");
         words.add(".html");
         words.add("电话:");
         words.add("qq:");
         words.add("msn:");
         words.add("网址:");
         words.add("手机:");
         words.add("邮箱:");
         words.add("联系人:");
         Review_Filter_Ads rfa = new Review_Filter_Ads(words,regs);
         Review_Filter_Few_Words rffw = new Review_Filter_Few_Words(15,15,0.7);
        
         String source_dir = "";
         String target_dir = "";
        
         
      
        
        

        
    }

}
