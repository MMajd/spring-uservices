
/** 
 * @link https://leetcode.com/problems/decode-string/
 */ 

class Solution {
    int i = 0; 
    public String decodeString(String str) {
        StringBuilder sb = new StringBuilder(); 
        int count = 0; 
        
        while(i<str.length()) {
            char c = str.charAt(i++);
            
            if (c == '[') { 
                String x = decodeString(str); /** dfs part */ 
                for (int i=0; i<count; i++) { 
                    sb.append(x);
                }
                count = 0; 
            }
            else if (Character.isAlphabetic(c)) { 
                sb.append(c); 
            } 
            else if (c == ']') { 
                break;
            }
            else { 
                count = count*10+c-'0'; 
            }
        }
        return sb.toString();
    }
}
