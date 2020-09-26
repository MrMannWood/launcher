package com.example.testapp;

public class TextUtil {

    public static int dist(String s1, String s2 ) {

        // memoize only previous line of distance matrix
        int[] prev = new int[ s2.length() + 1 ];

        for( int j = 0; j < s2.length() + 1; j++ ) {
            prev[ j ] = j;
        }

        for( int i = 1; i < s1.length() + 1; i++ ) {

            // calculate current line of distance matrix
            int[] curr = new int[ s2.length() + 1 ];
            curr[0] = i;

            for( int j = 1; j < s2.length() + 1; j++ ) {
                int d1 = prev[ j ] + 1;
                int d2 = curr[ j - 1 ] + 1;
                int d3 = prev[ j - 1 ];
                if ( s1.charAt(i - 1) != s2.charAt(j - 1)) {
                    d3 += 1;
                }
                curr[ j ] = Math.min( Math.min( d1, d2 ), d3 );
            }

            // define current line of distance matrix as previous
            prev = curr;
        }
        return prev[ s2.length() ];
    }
}
