package com.douding.file.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class test {
    public int[] twoSum(int[] nums, int target) {
    HashMap<Integer,Integer> map = new HashMap<>();
    for(int i =0;i<nums.length;i++){
        if(map.containsKey(target-nums[i])){
            return new int[]{map.get(target-nums[i]),i};
        }
        map.put(target-nums[i],i);
    }
    return  new int[0];
    }

    public boolean isPalindrome(int x) {
        List<Integer> list = new ArrayList<Integer>();
        Math.abs(x);
        String str=x+"";
        StringBuffer s = new StringBuffer(str);
        s.reverse();
        String s1=s.toString();
        if (str.equals(s1)){
            return true;
        }
        return false;

    }

}
