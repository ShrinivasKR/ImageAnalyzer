Shrinivas Kopparam Ramanath and shrini 1167430

 1. Which extra credit options you implemented, if any.

    None.

 2. Brief explanation of your hash function h3 for blocks and why you think
    it should do a good job in terms of (a) key scattering and (b) efficiency.

    For my third hashing method, I created a string using each block's R, G, and B value added together using concatenation.
    I then turned the string into hash code utilizing the hashCode method of the string object.
    This should work because it produces a unique hash for each block with different color, which also produces the same hash code for other blocks with the same color.
    The same color is inputted multiple times.
    In terms of key scattering, it is efficient as long as too many of the same RGB values aren't inputted, which will result in collisions.
    In terms of the hashing efficiency, it's pretty damn efficient as the method is simple.


 3. An answer to the question: "Why does the fast method for encoding run more
    quickly than the Slow and Simple method?"

    The fast method is faster than the slow method because the euclidean distance is calculated n times for the number of unique blocks.
    In the slow method, however, the euclidean distance is calculated more times than the fast method, for each individual pixel and color in palette and it involves complex mathematical calculations.
    The fast method only calculates the euclidean distance only for the number of unique colors in the image whereas the slow method calculates it for every block.

 4. An answer to the question: "What kind of compression ratio can one
    expect from indexed color encoding, assuming one is willing to
    accept a slightly noticeable distortion in the colors of an image?"

    The higher the compression ratio, the more noticeable the  distortion in the colors on the image.
    When the compression ratio is lower, there is less distortion in the colors of the image.
    The more adept someone is at noticing image defects, the faster they will notice distortion in the colors of the image as the compression ratio is increased,