
/** 
 * @link https://leetcode.com/problems/removing-minimum-and-maximum-from-array/
 *
 */ 

class Solution {
    public int minimumDeletions(int[] nums) {
        int min=0, max=0; 
        int N = nums.length; 
        
        for (int i=0; i<N; i++) { 
            if (nums[i] > nums[max]) { 
                max = i; 
            } 
            else if (nums[i] < nums[min]) { 
                min = i; 
            }
        }
        
        int back = Math.max(N-min,N-max);
        int front = Math.max(max+1, min+1);
        int both = Math.min(N-min,min+1)+Math.min(N-max,max+1);
        
        return Math.min(Math.min(back,front),both);
    }
}
