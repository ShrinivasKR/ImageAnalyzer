/*
 * ImageAnalyzer.java
 * Starter code for A3 // A3 Solution by Shrinivas Kopparam Ramanath  shrini 1167430.
 * 
 * 
 * See also the file CustomHashtable.java for use in the extra credit options.
 * CSE 373, University of Washington, Autumn 2014.
 * 
 * Starter Code for CSE 373 Assignment 3, Part II.    Starter Code Version 1.0.
 * S. Tanimoto,  with contributions from J. Goh, Oct 21, 2014.
 * 
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageAnalyzer extends JFrame implements ActionListener {
    public static ImageAnalyzer appInstance; // Used in main().

    String startingImage = "UW-Campus-1961.jpg";
    BufferedImage biTemp, biWorking, biFiltered; // These hold arrays of pixels.
    Graphics gOrig, gWorking; // Used to access the drawImage method.
    int w; // width of the current image.
    int h; // height of the current image.
    int blockSize; // Controls how much colors are grouped during palette building.
    int hashFunctionChoice; // Either 1, 2, or 3. Controls whether to use h1, h2, or h3.

    public HashMap<Block, Integer> javaHashMap; // For storing blocks:weights
    public CustomHashtable<Block, Integer> customHashtable; // Used only if extra credit options are implemented.

    public ArrayList<Block> sortedBlocks; // to store sorted blocks(list L)
    public Color[] palette;	// stores the first U elements of list L
    public int[][] encodedPixels;	// to store the value each pixel in the image is encoded to
    long timeTaken;

    JPanel viewPanel; // Where the image will be painted.
    JPopupMenu popup;
    JMenuBar menuBar;
    JMenu fileMenu, imageOpMenu, paletteMenu, encodeMenu, hashingMenu, helpMenu;
    JMenuItem loadImageItem, saveAsItem, exitItem;
    JMenuItem lowPassItem, highPassItem, photoNegItem, RGBThreshItem;
    JMenuItem createPItem2, createPItem4, createPItem16, createPItem256, selectBItem4, selectBItem8, selectBItem16;
    JMenuItem encodeSSItem, encodeFItem, decodeItem;
    JMenuItem hashFunctionItem1, hashFunctionItem2, hashFunctionItem3;
    JMenuItem hashtableItem1, hashtableItem2, hashtableItem3;
    JMenuItem aboutItem, helpItem;

    JFileChooser fileChooser; // For loading and saving images.

    /**
     * Color object  of rgb values
     */
    public class Color {
        int r, g, b;

        /**
         * Constructor method for Color Object
         * @param r value for red
         * @param g value for green
         * @param b value for blue
         */
        Color(int r, int g, int b) {
            this.r = r; this.g = g; this.b = b;
        }

        /**
         * Calculates the euclidean distance between two color objects
         * @param c2 a different color object
         * @return the euclidean distance between this color object and the other color object being passed in
         */
        double euclideanDistance(Color c2) {
            // TODO
            // Replace this to return the distance between this color and c2.
            int differenceRed = r - c2.r;
            int differenceGreen = g - c2.g;
            int differenceBlue = b - c2.b;
            return Math.sqrt((Math.pow(differenceRed, 2)  + Math.pow(differenceGreen, 2) + Math.pow(differenceBlue, 2)));
        }
    }

    /**
     *
     */
    public class Block {
        // TODO
        // Implement your block methods here.  hashCode is done for you, but you still need to write
        // the bodies for h1, h2, and h3 near the end of this file.
        // Include necessary fields, constructor and remember to override the equals and toString methods.

        /**
         * variables for red, green and blue in block object
         */
        private int red, green, blue;

        /**
         * Constructor method of Block object
         * @param red for value of red
         * @param green for value of green
         * @param blue for value of blue
         */
        public Block (int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        /**
         * Provides value of red
         * @return int red
         */
        public int getRed() {
            return red;
        }

        /**
         * Provides value of green
         * @return int green
         */
        public int getGreen() {
            return green;
        }

        /**
         * Provides value of blue
         * @return int blue
         */
        public int getBlue() {
            return blue;
        }

        public int hashCode() {
            if (hashFunctionChoice == 1) {
                return h1(this);
            } else if (hashFunctionChoice == 2) {
                return h2(this);
            } else if (hashFunctionChoice == 3) {
                return h3(this);
            } else {
                return -1; // This should never happen.
            }
        }

        /**
         * Checks if this block is equal to the other block passed in
         * @param givenObject the other block
         * @return boolean true if equal and false if not
         */
        public boolean equals(Object givenObject) {
            return givenObject != null && givenObject.getClass() == getClass()
                    && green == ((Block)givenObject).getGreen()
                    && red == ((Block)givenObject).getRed()
                    && blue == ((Block)givenObject).getBlue();
        }

        /**
         * A method to represent the Block in String format
         * @return String representation of Block
         */
        public String toString() {
            return ("Red Value: " + red + ", Green Value: " + green + ", Blue Value: " + blue);
        }
    }

    // Some image manipulation data definitions that won't change...
    static LookupOp PHOTONEG_OP, RGBTHRESH_OP;
    static ConvolveOp LOWPASS_OP, HIGHPASS_OP;

    public static final float[] SHARPENING_KERNEL = { // sharpening filter kernel
            0.f, -1.f,  0.f,
            -1.f,  5.f, -1.f,
            0.f, -1.f,  0.f
    };

    public static final float[] BLURRING_KERNEL = {
            0.1f, 0.1f, 0.1f,    // low-pass filter kernel
            0.1f, 0.2f, 0.1f,
            0.1f, 0.1f, 0.1f
    };

    public ImageAnalyzer() { // Constructor for the application.
        setTitle("Image Analyzer");
        addWindowListener(new WindowAdapter() { // Handle any window close-box clicks.
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });

        // Create the panel for showing the current image, and override its
        // default paint method to call our paintPanel method to draw the image.
        viewPanel = new JPanel(){public void paint(Graphics g) { paintPanel(g);}};
        add("Center", viewPanel); // Put it smack dab in the middle of the JFrame.

        // Create standard menu bar
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        fileMenu = new JMenu("File");
        imageOpMenu = new JMenu("Image Operations");
        paletteMenu = new JMenu("Palettes");
        encodeMenu = new JMenu("Encode");
        hashingMenu = new JMenu("Hashing");
        helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(imageOpMenu);
        menuBar.add(paletteMenu);
        menuBar.add(encodeMenu);
        menuBar.add(hashingMenu);
        menuBar.add(helpMenu);

        // Create the File menu's menu items.
        loadImageItem = new JMenuItem("Load image...");
        loadImageItem.addActionListener(this);
        fileMenu.add(loadImageItem);
        saveAsItem = new JMenuItem("Save as full-color PNG");
        saveAsItem.addActionListener(this);
        fileMenu.add(saveAsItem);
        exitItem = new JMenuItem("Quit");
        exitItem.addActionListener(this);
        fileMenu.add(exitItem);

        // Create the Image Operation menu items.
        lowPassItem = new JMenuItem("Convolve with blurring kernel");
        lowPassItem.addActionListener(this);
        imageOpMenu.add(lowPassItem);
        highPassItem = new JMenuItem("Convolve with sharpening kernel");
        highPassItem.addActionListener(this);
        imageOpMenu.add(highPassItem);
        photoNegItem = new JMenuItem("Photonegative");
        photoNegItem.addActionListener(this);
        imageOpMenu.add(photoNegItem);
        RGBThreshItem = new JMenuItem("RGB Thresholds at 128");
        RGBThreshItem.addActionListener(this);
        imageOpMenu.add(RGBThreshItem);

        // Create the Palette menu items.
        createPItem2 = new JMenuItem("Create Palette of Size 2");
        createPItem2.addActionListener(this);
        paletteMenu.add(createPItem2);
        createPItem4 = new JMenuItem("Create Palette of Size 4");
        createPItem4.addActionListener(this);
        paletteMenu.add(createPItem4);
        createPItem16 = new JMenuItem("Create Palette of Size 16");
        createPItem16.addActionListener(this);
        paletteMenu.add(createPItem16);
        createPItem256 = new JMenuItem("Create Palette of Size 256");
        createPItem256.addActionListener(this);
        paletteMenu.add(createPItem256);
        selectBItem4 = new JCheckBoxMenuItem("Set block size to 4x4x4", true);
        selectBItem4.addActionListener(this);
        paletteMenu.add(selectBItem4);
        selectBItem8 = new JCheckBoxMenuItem("Set block size to 8x8x8");
        selectBItem8.addActionListener(this);
        paletteMenu.add(selectBItem8);
        selectBItem16 = new JCheckBoxMenuItem("Set block size to 16x16x16");
        selectBItem16.addActionListener(this);
        paletteMenu.add(selectBItem16);

        // Create the Encode menu items.
        encodeSSItem = new JMenuItem("Encode: Slow and Simple");
        encodeSSItem.addActionListener(this);
        encodeMenu.add(encodeSSItem);
        encodeSSItem.setEnabled(false);

        encodeFItem = new JMenuItem("Encode: Fast");
        encodeFItem.addActionListener(this);
        encodeMenu.add(encodeFItem);
        encodeFItem.setEnabled(false);

        decodeItem = new JMenuItem("Decode");
        decodeItem.addActionListener(this);
        encodeMenu.add(decodeItem);
        decodeItem.setEnabled(false);

        // Create the Hashing menu items.
        hashFunctionItem1 = new JCheckBoxMenuItem("Use Hash Function H1", true);
        hashFunctionItem1.addActionListener(this);
        hashingMenu.add(hashFunctionItem1);

        hashFunctionItem2 = new JCheckBoxMenuItem("Use Hash Function H2");
        hashFunctionItem2.addActionListener(this);
        hashingMenu.add(hashFunctionItem2);

        hashFunctionItem3 = new JCheckBoxMenuItem("Use Hash Function H3");
        hashFunctionItem3.addActionListener(this);
        hashingMenu.add(hashFunctionItem3);

        hashtableItem1 = new JCheckBoxMenuItem("Use Java's Hashtable class.", true);
        hashtableItem1.addActionListener(this);
        hashingMenu.add(hashtableItem1);
        hashtableItem1.setEnabled(false);

        hashtableItem2 = new JCheckBoxMenuItem("Use custom hashtable class and linear probing.");
        hashtableItem2.addActionListener(this);
        hashingMenu.add(hashtableItem2);
        hashtableItem2.setEnabled(false);

        hashtableItem3 = new JCheckBoxMenuItem("Use custom hashtable class and quadratic probing.");
        hashtableItem3.addActionListener(this);
        hashingMenu.add(hashtableItem3);
        hashtableItem3.setEnabled(false);

        // Create the Help menu's item.
        aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(this);
        helpMenu.add(aboutItem);
        helpItem = new JMenuItem("Help");
        helpItem.addActionListener(this);
        helpMenu.add(helpItem);

        // Initialize the image operators, if this is the first call to the constructor:
        if (PHOTONEG_OP==null) {
            byte[] lut = new byte[256];
            for (int j=0; j<256; j++) {
                lut[j] = (byte)(256-j);
            }
            ByteLookupTable blut = new ByteLookupTable(0, lut);
            PHOTONEG_OP = new LookupOp(blut, null);
        }
        if (RGBTHRESH_OP==null) {
            byte[] lut = new byte[256];
            for (int j=0; j<256; j++) {
                lut[j] = (byte)(j < 128 ? 0: 200);
            }
            ByteLookupTable blut = new ByteLookupTable(0, lut);
            RGBTHRESH_OP = new LookupOp(blut, null);
        }
        if (LOWPASS_OP==null) {
            float[] data = BLURRING_KERNEL;
            LOWPASS_OP = new ConvolveOp(new Kernel(3, 3, data),
                    ConvolveOp.EDGE_NO_OP,
                    null);
        }
        if (HIGHPASS_OP==null) {
            float[] data = SHARPENING_KERNEL;
            HIGHPASS_OP = new ConvolveOp(new Kernel(3, 3, data),
                    ConvolveOp.EDGE_NO_OP,
                    null);
        }
        hashFunctionChoice = 1; // Default hash function number is 1.
        blockSize = 4; // Default blockSize is 4x4x4
        loadImage(startingImage); // Read in the pre-selected starting image.
        setVisible(true); // Display it.
    }

    /*
     * Given a path to a file on the file system, try to load in the file
     * as an image.  If that works, replace any current image by the new one.
     * Re-make the biFiltered buffered image, too, because its size probably
     * needs to be different to match that of the new image.
     */
    public void loadImage(String filename) {
        try {
            biTemp = ImageIO.read(new File(filename));
            w = biTemp.getWidth();
            h = biTemp.getHeight();
            viewPanel.setSize(w,h);
            biWorking = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            gWorking = biWorking.getGraphics();
            gWorking.drawImage(biTemp, 0, 0, null);
            biFiltered = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            pack(); // Lay out the JFrame and set its size.
            repaint();
        } catch (IOException e) {
            System.out.println("Image could not be read: "+filename);
            System.exit(1);
        }
    }

    /* Menu handlers
     */
    void handleFileMenu(JMenuItem mi){
        System.out.println("A file menu item was selected.");
        if (mi==loadImageItem) {
            File loadFile = new File("image-to-load.png");
            if (fileChooser==null) {
                fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(loadFile);
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", new String[] { "JPG", "JPEG", "GIF", "PNG" }));
            }
            int rval = fileChooser.showOpenDialog(this);
            if (rval == JFileChooser.APPROVE_OPTION) {
                loadFile = fileChooser.getSelectedFile();
                loadImage(loadFile.getPath());
            }
        }
        if (mi==saveAsItem) {
            File saveFile = new File("savedimage.png");
            fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(saveFile);
            int rval = fileChooser.showSaveDialog(this);
            if (rval == JFileChooser.APPROVE_OPTION) {
                saveFile = fileChooser.getSelectedFile();
                // Save the current image in PNG format, to a file.
                try {
                    ImageIO.write(biWorking, "png", saveFile);
                } catch (IOException ex) {
                    System.out.println("There was some problem saving the image.");
                }
            }
        }
        if (mi==exitItem) { this.setVisible(false); System.exit(0); }
    }

    void handleEditMenu(JMenuItem mi){
        System.out.println("An edit menu item was selected.");
    }

    void handleImageOpMenu(JMenuItem mi){
        System.out.println("An imageOp menu item was selected.");
        if (mi==lowPassItem) { applyOp(LOWPASS_OP); }
        else if (mi==highPassItem) { applyOp(HIGHPASS_OP); }
        else if (mi==photoNegItem) { applyOp(PHOTONEG_OP); }
        else if (mi==RGBThreshItem) { applyOp(RGBTHRESH_OP); }
        repaint();
    }

    /**
     * Enables and disables Palette Menu Items as required using helper methods and sends the command to build a palette when the menu item is selected
     * @param mi
     */
    void handlePaletteMenu(JMenuItem mi){
        System.out.println("A palette menu item was selected.");
        if (mi==createPItem2) {
            buildPalette(2);
            enableEncodeMenuItems();
        } // TODO Call your method here.
        else if (mi==createPItem4) {
            buildPalette(4);
            enableEncodeMenuItems();
        } // TODO Call your method here.
        else if (mi==createPItem16) {
            buildPalette(16);
            enableEncodeMenuItems();
        } // TODO Call your method here.
        else if (mi==createPItem256) {
            buildPalette(256);
            enableEncodeMenuItems();
        } // TODO Call your method here.
        else if (mi==selectBItem4) {
            setBlockSize(4);
            disableEncodeDecodeMenuItems();
        }
        else if (mi==selectBItem8) {
            setBlockSize(8);
            disableEncodeDecodeMenuItems();
        }
        else if (mi==selectBItem16) {
            setBlockSize(16);
            disableEncodeDecodeMenuItems();
        }

    }

    /**
     * Enables and disables the Encode Menu Items as needed using helper methods and also sends the command to encode or decode when a menu item is selected
     * @param mi
     */
    void handleEncodeMenu(JMenuItem mi){
        System.out.println("An encode menu item was selected.");
        if (mi==encodeSSItem){
            encodeSlowAndSimple();
            disableEncodeMenuItems();
        } // TODO Call your method here.
        else if (mi==encodeFItem) {
            encodeFast();
            disableEncodeMenuItems();
        } // TODO Call your method here.
        else if (mi==decodeItem) {
            decode();
            enableEncodeMenuItems();
        } // TODO Call your method here.
    }

    void handleHashingMenu(JMenuItem mi){
        System.out.println("A hashing menu item was selected.");
        if (mi==hashFunctionItem1) { setHashFunctionChoice(1); }
        else if (mi==hashFunctionItem2) { setHashFunctionChoice(2); }
        else if (mi==hashFunctionItem3) { setHashFunctionChoice(3); }
    }

    void handleHelpMenu(JMenuItem mi){
        System.out.println("A help menu item was selected.");
        if (mi==aboutItem) {
            System.out.println("About: Well this is my program.");
            JOptionPane.showMessageDialog(this,
                    "Image Analyzer, Starter-Code Version.",
                    "About",
                    JOptionPane.PLAIN_MESSAGE);
        }
        else if (mi==helpItem) {
            System.out.println("In case of panic attack, select File: Quit.");
            JOptionPane.showMessageDialog(this,
                    "To load a new image, choose File: Load image...\nFor anything else, just try different things.",
                    "Help",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    /*
     * Used by Swing to set the size of the JFrame when pack() is called.
     */
    public Dimension getPreferredSize() {
        return new Dimension(w, h+50); // Leave some extra height for the menu bar.
    }

    public void paintPanel(Graphics g) {
        g.drawImage(biWorking, 0, 0, null);
    }

    public void applyOp(BufferedImageOp operation) {
        operation.filter(biWorking, biFiltered);
        gWorking.drawImage(biFiltered, 0, 0, null);
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource(); // What Swing object issued the event?
        if (obj instanceof JMenuItem) { // Was it a menu item?
            JMenuItem mi = (JMenuItem)obj; // Yes, cast it.
            JPopupMenu pum = (JPopupMenu)mi.getParent(); // Get the object it's a child of.
            JMenu m = (JMenu) pum.getInvoker(); // Get the menu from that (popup menu) object.
            //System.out.println("Selected from the menu: "+m.getText()); // Printing this is a debugging aid.

            if (m==fileMenu)    { handleFileMenu(mi);    return; }  // Handle the item depending on what menu it's from.
            if (m==imageOpMenu) { handleImageOpMenu(mi); return; }
            if (m==paletteMenu) { handlePaletteMenu(mi); return; }
            if (m==encodeMenu)  { handleEncodeMenu(mi);  return; }
            if (m==hashingMenu) { handleHashingMenu(mi); return; }
            if (m==helpMenu)    { handleHelpMenu(mi);    return; }
        } else {
            System.out.println("Unhandled ActionEvent: "+e.getActionCommand());
        }
    }

    /**
     * Changes hash function item check states and displays which hash function is selected
     * @param hc
     */
    public void setHashFunctionChoice(int hc) {
        hashFunctionChoice = hc;
        System.out.println("Hash function choice is now "+hashFunctionChoice);
        // TODO
        // Updates the menu item states appropriately.
        switch (hc) {
            case 1:
                hashFunctionItem1.setSelected(true);
                hashFunctionItem2.setSelected(false);
                hashFunctionItem3.setSelected(false);
                break;
            case 2:
                hashFunctionItem2.setSelected(true);
                hashFunctionItem1.setSelected(false);
                hashFunctionItem3.setSelected(false);
                break;
            case 3:
                hashFunctionItem3.setSelected(true);
                hashFunctionItem1.setSelected(false);
                hashFunctionItem2.setSelected(false);
                break;
        }
    }

    public void setBlockSize(int bs) {
        blockSize = bs;
        // TODO
        // Updates the menu item states appropriately.
        switch (bs) {
            case 4:
                selectBItem4.setSelected(true);
                selectBItem8.setSelected(false);
                selectBItem16.setSelected(false);
                break;
            case 8:
                selectBItem8.setSelected(true);
                selectBItem4.setSelected(false);
                selectBItem16.setSelected(false);
                break;
            case 16:
                selectBItem16.setSelected(true);
                selectBItem4.setSelected(false);
                selectBItem8.setSelected(false);
                break;
        }
    }

    /**
     * Creates a palette using the Popularity Algorithm and sorts it by weight
     * @param paletteSize
     */
    public void buildPalette(int paletteSize) {
        // TODO
        // You may use the sort function defined below to help sort a HashMap<Block, Integer>.
        // Comment each step.
        timeTaken = 0; //Initializes the total time taken for execution to 0
        long startTime = System.nanoTime(); //starts timing execution
        javaHashMap = new HashMap<Block, Integer>(); //initializes the javaHashMap as an empty HashMap
        Color[][] imagePixels = storeCurrPixels(biWorking); //saves the pixels of the current working image
        for (Color[] pixelRow: imagePixels) { //goes through the row of pixels
            for (Color currPixel: pixelRow) { //goes through each pixel in the row
                Block currBlock = new Block(currPixel.r / blockSize, currPixel.g / blockSize, currPixel.b / blockSize); //creates a block for the pixel
                if (javaHashMap.containsKey(currBlock)) { //check if the hash map contains the current block
                    int lastValue = javaHashMap.get(currBlock).intValue(); //gets the current weight of the block
                    javaHashMap.put(currBlock, Integer.valueOf(lastValue + 1)); //increases the weight by one if the block is already here
                } else { //if the block is not contained
                    javaHashMap.put(currBlock, Integer.valueOf(1)); //places the block with weight of one
                }
            }
        }
        sortedBlocks = sort(javaHashMap); //sorts the hash map by weight
        if (sortedBlocks.size() < paletteSize) { //checks if the unique blocks is fewer than palette size
            palette = new Color[sortedBlocks.size()]; //creates a new palette of size the number of unique blocks
        } else { //if the unique blocks is not fewer than palette size
            palette = new Color[paletteSize]; //creates a new palette of size the given palette size if colors are there
        }
        for (int i = 0; i < palette.length; i++) { //goes through the palette
            Block currBlock = sortedBlocks.get(i); //gets the current block
            int halfBlockSize = blockSize / 2; //finds half the current block's size
            palette[i] = new Color(currBlock.getRed() * blockSize + halfBlockSize, //calculates the representative color
                    currBlock.getGreen() * blockSize + halfBlockSize,
                    currBlock.getBlue() * blockSize + halfBlockSize);
        }
        long timeTaken = (System.nanoTime() - startTime) / 1000000; //calculates the time taken to build the palette
        this.timeTaken = timeTaken; //sets it to the total time taken in execution so far
        System.out.println("Elapsed time in milliseconds to build the table: " + timeTaken); //prints the time taken to build the table
        printStats(); //prints the rest of the statistics using a helper method
    }

    // returns a sorted(largest weight to smallest weight) ArrayList of the blocks in HashMap<Block, Integer>
    ArrayList<Block> sort(final HashMap<Block, Integer> map) {
        ArrayList<Block> arr = new ArrayList<Block>();
        for (Block b : map.keySet()) {
            arr.add(b);
        }
        Collections.sort(arr, new Comparator<Block> (){
            public int compare(Block b1, Block b2) {
                return map.get(b2) - map.get(b1);
            }
        });
        return arr;
    }

    public void encodeSlowAndSimple() {
        // TODO
        // Add your code here to determine the encoded pixel values and store them in the array encodedPixels (first method).
        long startTime = System.nanoTime();
        Color[][] currentPixels = storeCurrPixels(biWorking);
        encodedPixels = new int[h][w];
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                Color currentPixel = currentPixels[row][col];
                int indexWithClosestColor = 0;
                double closestColorDistance = currentPixel.euclideanDistance(palette[0]);
                for (int colorIndex = 1; colorIndex < palette.length; colorIndex++) {
                    //Finds the distance between current pixel and palette color
                    double colorDist = currentPixel.euclideanDistance(palette[colorIndex]);
                    //Checks if the color is already the closest
                    if (colorDist < closestColorDistance) {
                        indexWithClosestColor = colorIndex;
                        closestColorDistance = colorDist;
                    }
                }
                encodedPixels[row][col] = indexWithClosestColor;
            }
        }
        long timeTaken = (System.nanoTime() - startTime) / 1000000;
        this.timeTaken += timeTaken;
        System.out.println("Elapsed time in milliseconds to encode the pixels slowly: " + timeTaken);
        printStats();
    }

    public void encodeFast() {
        // TODO
        // Add your code here to determine the encoded pixel values and store them in the array encodedPixels (second method, using sortedBlocks and/or javaHashMap again).
        long startTime = System.nanoTime();
        Set<Block> blocksInHash= javaHashMap.keySet();
        // The weight for each block is replaced by the closest palette color's index
        for(Block colorBlock: blocksInHash) {
            int halfBlockSize = blockSize / 2;
            Color representativeColor = new Color(colorBlock.getRed() * blockSize + halfBlockSize,
                    colorBlock.getGreen() * blockSize + halfBlockSize,
                    colorBlock.getBlue() * blockSize + halfBlockSize);
            int closestColorIndex = 0;
            double closestColorDistance = representativeColor.euclideanDistance(palette[0]);
            for (int index = 1; index < palette.length; index++) {
                double colorDistance = representativeColor.euclideanDistance(palette[index]);
                if(closestColorDistance > colorDistance) {
                    closestColorIndex = index;
                    closestColorDistance = colorDistance;
                }
            }
            javaHashMap.put(colorBlock, Integer.valueOf(closestColorIndex));
        }
        Color[][] currentPixels = storeCurrPixels(biWorking);
        encodedPixels = new int[h][w];
        //Represents the pixels with its closest palette pixels
        for (int row = 0; row < h; row ++) {
            for (int col = 0; col < w; col++) {
                Color currentPixel = currentPixels[row][col];
                Block currentPixelBlock = new Block(currentPixel.r / blockSize, currentPixel.g / blockSize, currentPixel.b / blockSize);
                encodedPixels[row][col] = javaHashMap.get(currentPixelBlock);
            }
        }
        long endTime = System.nanoTime();
        long timeTaken = (endTime - startTime) / 1000000;
        this.timeTaken += timeTaken;
        System.out.println("Elapsed time in milliseconds to encode the pixels quickly: " + timeTaken);
        printStats();
    }

    public void decode() {
        long startTime = System.nanoTime();
        Color[][] originalPixels = storeCurrPixels(biWorking);
        // TODO
        // Add your code here to determine RGB values for each pixel from the encoded information, and
        // put the RGB information into biWorking.
        // Use the putPixel function defined below to store a color into a pixel
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                putPixel(biWorking, col, row, palette[encodedPixels[row][col]]);
            }
        }
        repaint();
        double averageEncodingError = computeError(originalPixels, biWorking);
        System.out.println("Average encoding error: " + averageEncodingError);
        long timeTaken = (System.nanoTime() - startTime) / 1000000;
        this.timeTaken += timeTaken;
        printOverallStatistics();
        this.timeTaken = 0;
    }

    // Returns an array of Colors based on the pixels from a BufferedImage
    Color[][] storeCurrPixels(BufferedImage bi) {
        Color[][] pixels = new Color[h][w];
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int rgb = bi.getRGB(col, row);

                int red = (rgb & 0x00ff0000) >> 16;
                int green = (rgb & 0x0000ff00) >> 8;
                int blue = rgb & 0x000000ff;

                pixels[row][col] = new Color(red, green, blue);
            }
        }
        return pixels;
    }

    // Computes the average pixel encoding error between a pixel array and the pixels in a BufferedImage
    double computeError(Color[][] pixels, BufferedImage bi) {
        double totalError = 0.0;
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int rgb = bi.getRGB(col, row);

                int red = (rgb & 0x00ff0000) >> 16;
                int green = (rgb & 0x0000ff00) >> 8;
                int blue = rgb & 0x000000ff;

                totalError += pixels[row][col].euclideanDistance(new Color(red, green, blue));
            }
        }
        return totalError / (h * w);
    }

    // Use this to put a color into a pixel of a BufferedImage object.
    void putPixel(BufferedImage bi, int x, int y, Color c) {
        int rgb = (c.r << 16) | (c.g << 8) | c.b; // pack 3 bytes into a word.
        bi.setRGB(x,  y, rgb);
    }

    public int h1(Block b) {
        // TODO
        // Replace this with your code
        return b.getRed() ^ b.getGreen() ^ b.getBlue();
    }

    public int h2(Block b) {
        // TODO
        // Replace this with your code
        return 1024 * b.getRed() + 32 * b.getGreen() + b.getBlue();
    }

    public int h3(Block b) {
        // TODO
        // Replace this with your code
        String stringToBeHashed = "" + b.getRed() + b.getGreen() + b.getBlue();
        return stringToBeHashed.hashCode();
    }

    /**
     * Helper method to enable encode menu items and disable the decode menu item when decode option is used
     */
    private void enableEncodeMenuItems() {
        encodeFItem.setEnabled(true);
        encodeSSItem.setEnabled(true);
        decodeItem.setEnabled(false);
    }

    /**
     * Helper method to disable encode menu items and enable the decode menu item when decode option is used
     */
    private void disableEncodeMenuItems() {
        encodeFItem.setEnabled(false);
        encodeSSItem.setEnabled(false);
        decodeItem.setEnabled(true);
    }

    /**
     * Helper method to disable encode menu items
     */
    private void disableEncodeDecodeMenuItems() {
        encodeFItem.setEnabled(false);
        encodeSSItem.setEnabled(false);
        decodeItem.setEnabled(false);
    }

    /**
     * Helper method to print Statistics
     */
    private void printStats() {
        System.out.println("Current Hashing Function: " + hashFunctionChoice);
        System.out.println("Number of pixels in image: " + w * h);
        System.out.println("Number of distinct bins: " + javaHashMap.size());
    }

    /**
     * Helper method to print total Statistics
     */
    private void printOverallStatistics() {
        System.out.println("Total time elapsed in milliseconds: " + timeTaken);
        int bitCountForPalette = Integer.bitCount(palette.length);
        int bitsForCompressedImage = bitCountForPalette * w * h  + 24 * palette.length;
        int bitsForOriginalImage = 24 * w * h;
        System.out.println("Compression Ratio: " + ((double) bitsForOriginalImage) / bitsForCompressedImage);
    }

    /* This main method can be used to run the application. */
    public static void main(String s[]) {
        appInstance = new ImageAnalyzer();
    }
}
