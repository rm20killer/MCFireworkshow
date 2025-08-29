package dev.rm20.mcfireworkshow.show.Effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import com.destroystokyo.paper.ParticleBuilder;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextEffects {

    // --- Configuration for AWT Font Rendering ---
    /**
     * Color used to draw the text onto the internal bitmap.
     */
    private static final Color DEFAULT_AWT_TEXT_COLOR = Color.BLACK;

    // --- Caching for Rendered Character Bitmaps ---
    // Cache: Font -> Character -> CharacterBitmapData
    private static final Map<Font, Map<Character, CharacterBitmapData>> FONT_CACHE = new HashMap<>();

    /**
     * Helper class to store the generated bitmap and its original pixel dimensions from AWT rendering.
     */
    private static class CharacterBitmapData {
        final boolean[][] bitmap; // true = particle, false = no particle
        final int awtPixelWidth;  // Width of the character in AWT pixels at rendering size
        final int awtPixelHeight; // Height of the character in AWT pixels at rendering size

        CharacterBitmapData(boolean[][] bitmap, int awtPixelWidth, int awtPixelHeight) {
            this.bitmap = bitmap;
            this.awtPixelWidth = awtPixelWidth;
            this.awtPixelHeight = awtPixelHeight;
        }
    }

    // --- Particle and Character Spacing ---
    /**
     * Spacing in Minecraft blocks between the centers of adjacent "pixels" of the rendered AWT font.
     * This effectively scales the rendered AWT font in the world.
     */
    private static final double PARTICLE_SPACING = 0.15; // Smaller value = denser/larger text for same font render size
    /**
     * Spacing between characters, as a multiple of PARTICLE_SPACING.
     * This means the space is equivalent to INTER_CHAR_SPACING_FACTOR AWT-level "pixels".
     */
    private static final double INTER_CHAR_SPACING_FACTOR = 2.0; // e.g., 2 "AWT pixels" worth of space

    /**
     * Retrieves a cached CharacterBitmapData for a given character and font,
     * or generates and caches it if not present.
     *
     * @param character The character to render.
     * @param font      The AWT Font to use for rendering.
     * @return CharacterBitmapData containing the bitmap and original dimensions.
     */
    private static CharacterBitmapData getOrCreateCharacterBitmap(char character, Font font) {
        // Ensure the outer map for the font exists
        FONT_CACHE.putIfAbsent(font, new HashMap<>());
        Map<Character, CharacterBitmapData> charMap = FONT_CACHE.get(font);

        // Return cached data if available
        if (charMap.containsKey(character)) {
            return charMap.get(character);
        }

        // 1. Create a temporary BufferedImage to get FontMetrics
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempG2d = tempImage.createGraphics();
        tempG2d.setFont(font);
        FontMetrics fm = tempG2d.getFontMetrics();

        // Determine character dimensions in AWT pixels
        int awtCharWidth = fm.charWidth(character);
        // Handle space character explicitly, as charWidth(' ') can be 0 or too small for some fonts
        if (character == ' ') {
            awtCharWidth = fm.charWidth('n') / 2; // Use half the width of 'n' as a typical space
            if (awtCharWidth == 0) awtCharWidth = font.getSize() / 3; // Fallback if 'n' is also 0
        }
        if (awtCharWidth <= 0) awtCharWidth = 1; // Ensure minimum 1 pixel width to avoid issues

        int awtCharHeight = fm.getHeight(); // Total height: ascent + descent + leading
        if (awtCharHeight <= 0) awtCharHeight = 1;

        tempG2d.dispose(); // Dispose of temporary graphics context

        // 2. Create the actual BufferedImage for rendering this character
        BufferedImage charImage = new BufferedImage(awtCharWidth, awtCharHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D charGraphics = charImage.createGraphics();

        // Set rendering hints for better quality (optional, but good for text)
        charGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        charGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        charGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        charGraphics.setFont(font);

        // Fill background (e.g., with white) - this helps if checking against non-background
        // charGraphics.setColor(DEFAULT_AWT_BACKGROUND_COLOR);
        // charGraphics.fillRect(0, 0, awtCharWidth, awtCharHeight);

        // Set text color and draw the character
        charGraphics.setColor(DEFAULT_AWT_TEXT_COLOR);
        // Draw string at (0, ascent) so the baseline is correct
        charGraphics.drawString(String.valueOf(character), 0, fm.getAscent());
        charGraphics.dispose(); // IMPORTANT: Dispose graphics to flush drawing

        // 3. Convert BufferedImage to boolean[][] bitmap
        // The bitmap dimensions are directly from the AWT rendered character image pixels.
        boolean[][] bitmap = new boolean[awtCharHeight][awtCharWidth];
        // int bgColorRGB = DEFAULT_AWT_BACKGROUND_COLOR.getRGB(); // If checking against background

        for (int y = 0; y < awtCharHeight; y++) {
            for (int x = 0; x < awtCharWidth; x++) {
                // A pixel is part of the character if its color is the text color.
                // More robust: check if alpha > threshold (e.g., if drawing on transparent background)
                // For drawing black text on transparent ARGB, check alpha: (charImage.getRGB(x, y) >> 24) != 0
                // If drawing black text on white, check if not white: charImage.getRGB(x, y) != bgColorRGB
                // Check if pixel is not fully transparent
                bitmap[y][x] = (charImage.getRGB(x, y) >> 24) != 0x00;
            }
        }

        CharacterBitmapData data = new CharacterBitmapData(bitmap, awtCharWidth, awtCharHeight);
        charMap.put(character, data); // Cache the result
        return data;
    }

    /**
     * Displays a string of text using particles (without rotation).
     * This calls the main rotational method with all angles set to 0.
     *
     * @param particleType The type of particle to use for drawing.
     * @param center       The Location where the text should be centered.
     * @param text         The string to display.
     * @param awtFont      The AWT Font to use for rendering the text.
     */
    public static void displayParticleText(Particle particleType, Location center, String text, Font awtFont) {
        displayParticleText(particleType, center, text, awtFont, 1, 0, 0, 0);
    }

    /**
     * Displays a string of text using particles, centered at the given location,
     * using the specified AWT Font and applying 3D rotation.
     *
     * @param particleType The type of particle to use for drawing (e.g., Particle.FLAME).
     * @param center       The Location where the text should be centered. This is the pivot point for rotation.
     * @param text         The string to display.
     * @param awtFont      The AWT Font to use for rendering the text.
     * @param size         The size factor for the text, affecting how large the text appears in the world.
     * @param yaw          Rotation around the Y-axis (in degrees).
     * @param pitch        Rotation around the X-axis (in degrees).
     * @param roll         Rotation around the Z-axis (in degrees).
     */
    public static void displayParticleText(Particle particleType, Location center, String text, Font awtFont, double size, double yaw, double pitch, double roll) {
        if (text == null || text.isEmpty() || center == null || center.getWorld() == null || particleType == null || awtFont == null) {
            System.err.println("TextEffects: Invalid parameters for displayParticleText.");
            return;
        }
        // Prevent rendering if size is zero or negative
        if (size <= 0) {
            return;
        }
        final double finalParticleSpacing = PARTICLE_SPACING * size;
        // --- First Pass: Calculate total dimensions and gather CharacterBitmapData ---
        List<CharacterBitmapData> charDataList = new ArrayList<>(text.length());
        double totalTextPixelWidth = 0;
        int maxLinePixelHeight = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            CharacterBitmapData cbd = getOrCreateCharacterBitmap(c, awtFont);
            charDataList.add(cbd);

            totalTextPixelWidth += cbd.awtPixelWidth;
            if (i < text.length() - 1) {
                totalTextPixelWidth += INTER_CHAR_SPACING_FACTOR;
            }
            maxLinePixelHeight = Math.max(maxLinePixelHeight, cbd.awtPixelHeight);
        }

        if (totalTextPixelWidth == 0) {
            return;
        }

        // Convert pixel dimensions to Minecraft block dimensions
        double totalDisplayTextWidthBlocks = totalTextPixelWidth * finalParticleSpacing;
        double actualLineHeightBlocks = maxLinePixelHeight * finalParticleSpacing;
        double actualInterCharSpacingBlocks = INTER_CHAR_SPACING_FACTOR * finalParticleSpacing;


        // --- Pre-calculate Rotation Trigonometry ---
        // Convert degrees to radians for Java's math functions
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double rollRad = Math.toRadians(roll);

        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);
        double cosRoll = Math.cos(rollRad);
        double sinRoll = Math.sin(rollRad);

//        World world = center.getWorld();
        double currentCharacterDrawX = 0; // The top-left X position of the character's bounding box

        // Second Pass: Calculate Rotated Positions and Draw Particles
        for (CharacterBitmapData cbd : charDataList) {
            boolean[][] charMatrix = cbd.bitmap;

            for (int row = 0; row < cbd.awtPixelHeight; row++) {
                for (int col = 0; col < cbd.awtPixelWidth; col++) {
                    if (charMatrix[row][col]) {
                        // 1. Calculate initial 2D position using the final spacing
                        double initialX = currentCharacterDrawX + (col * finalParticleSpacing);
                        double initialY = -(row * finalParticleSpacing);

                        // 2. Translate coordinates to be relative to the center of the text block
                        double relX = initialX - (totalDisplayTextWidthBlocks / 2.0);
                        double relY = initialY + (actualLineHeightBlocks / 2.0);
                        double relZ = 0;

                        // 3. Apply 3D rotations
                        double rotatedX_Yaw = relX * cosYaw + relZ * sinYaw;
                        double rotatedZ_Yaw = -relX * sinYaw + relZ * cosYaw;

                        double rotatedY_Pitch = relY * cosPitch - rotatedZ_Yaw * sinPitch;
                        double rotatedZ_Pitch = relY * sinPitch + rotatedZ_Yaw * cosPitch;

                        double finalRelX = rotatedX_Yaw * cosRoll - rotatedY_Pitch * sinRoll;
                        double finalRelY = rotatedX_Yaw * sinRoll + rotatedY_Pitch * cosRoll;
                        double finalRelZ = rotatedZ_Pitch;

                        // 4. Translate the rotated point to its final world position
                        Location particleLocation = center.clone().add(finalRelX, finalRelY, finalRelZ);

                        new ParticleBuilder(particleType)
                                .location(particleLocation)
                                .count(1)
                                .offset(0, 0, 0)
                                .receivers(248)
                                .extra(0)
                                .force(true)
                                .spawn();
                    }
                }
            }
            // Advance the X position for the next character
            currentCharacterDrawX += (cbd.awtPixelWidth * finalParticleSpacing) + actualInterCharSpacingBlocks;
        }
    }
}
