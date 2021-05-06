package educanet;

import educanet.models.Gamefield;
import educanet.models.Player;
import educanet.utils.FileUtils;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Game {

    public static void init(long window) {
        // Setup shaders
        Shaders.initShaders();
        prepareGamefield();

        createPlayer();
    }

    public static void render(long window) {
        renderPlayer(Player.getMatrix());
        renderGamefield();
    }

    public static void update(long window) {
        movePlayer(window, Player.getMatrix());
        checkCollision();
    }


    // PLAYER

    public static Player mainPlayer;
    public static float playerTopLeftX = -0.125f;
    public static float playerTopLeftY = 0.125f;

    public static void createPlayer() {
        mainPlayer = new Player();
    }

    public static void renderPlayer(Matrix4f matrix) {
        matrix.get(Player.getMatrixBuffer());

        GL33.glUniformMatrix4fv(Player.getUniformMatrixLocation(), false, Player.getMatrixBuffer());
        GL33.glUseProgram(Shaders.shaderProgramId); // use this shader to render
        GL33.glBindVertexArray(mainPlayer.getSquareVaoId());
        GL33.glDrawElements(GL33.GL_TRIANGLES, mainPlayer.getVertices().length, GL33.GL_UNSIGNED_INT, 0);
    }


    public static void movePlayer(long window, Matrix4f matrix) {
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) { // Move up
            if (playerTopLeftY < 1f) {
                matrix = matrix.translate(0, 0.0002f, 0f);
                playerTopLeftY += 0.0002f;
            }
        }
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) { // Move left
            if (playerTopLeftX > -1f) {
                matrix = matrix.translate(-0.0002f, 0f, 0f);
                playerTopLeftX -= 0.0002f;
            }
        }
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) { // Move down
            if (playerTopLeftY > -0.75f) {
                matrix = matrix.translate(0f, -0.0002f, 0f);
                playerTopLeftY -= 0.0002f;
            }
        }
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) { // Move right
            if (playerTopLeftX < 0.75f) {
                matrix = matrix.translate(0.0002f, 0f, 0f);
                playerTopLeftX += 0.0002f;
            }
        }
    }


    // GAMEFIELD

    public static ArrayList<Gamefield> gamefieldObjectArrayList = new ArrayList<>();

    public static String gameField;
    public static int numberOfObjectsGamefield;

    public static void renderGamefield() {
        Gamefield.matrix.get(Gamefield.matrixBuffer);
        int location = GL33.glGetUniformLocation(Shaders.shaderProgramId, "matrix");
        GL33.glUniformMatrix4fv(location, false, Gamefield.matrixBuffer);

        for (int i = 0; i < gamefieldObjectArrayList.size(); i++) {
            Gamefield g = gamefieldObjectArrayList.get(i);

            GL33.glUseProgram(Shaders.shaderProgramId); // use this shader to render
            GL33.glBindVertexArray(g.getVaoId());
            GL33.glDrawElements(GL33.GL_TRIANGLES, g.getVertices().length, GL33.GL_UNSIGNED_INT, 0);
        }
    }

    public static void prepareGamefield() { // read file, convert to verticies, add Squares to ArrayList
        String path = "src/main/gameResources/gamefield.txt";
        File level = new File(path);

        if (level.exists() && level.canRead()) {//checks if maze file exists and is readable
            gameField = FileUtils.readFile(path);
        }
        Matcher m = Pattern.compile("\r\n|\r|\n").matcher(gameField); //using matcher to not fill up gc with useless strings from .split();
        while (m.find()) {
            numberOfObjectsGamefield++;
        }

        String[] objs = gameField.split("\n");

        for (int i = 0; i < numberOfObjectsGamefield; i++) {
            String[] objAtrribs = objs[i].split(";");
            Gamefield g = new Gamefield();
            float[] newVerticies = {
                    Float.parseFloat(objAtrribs[0]) + Float.parseFloat(objAtrribs[2]), Float.parseFloat(objAtrribs[1]),                                   0, // top right
                    Float.parseFloat(objAtrribs[0]) + Float.parseFloat(objAtrribs[2]), Float.parseFloat(objAtrribs[1]) + Float.parseFloat(objAtrribs[2]), 0, // bottom right
                    Float.parseFloat(objAtrribs[0]),                                   Float.parseFloat(objAtrribs[1]) + Float.parseFloat(objAtrribs[2]), 0, // bottom left
                    Float.parseFloat(objAtrribs[0]),                                   Float.parseFloat(objAtrribs[1]),                                   0, // top left
            };
            g.setVertices(newVerticies);
            gamefieldObjectArrayList.add(g);
        }
    }

    static int topLeftX = 9;
    static int topLeftY = 10;
    static int topRightX = 0;
    static int topRightY = 1;
    static int botLeftX = 6;
    static int botLeftY = 7;
    static int botRightX = 3;
    static int botRightY = 4;

    static boolean collides = false;

    public static void checkCollision() {
        for (int i = 0; i < gamefieldObjectArrayList.size(); i++) {
            // for all 4 corner of player if x is in interval of (x left < corner < x right of object)
            Gamefield g = gamefieldObjectArrayList.get(i);

            float[] verticesObject = g.getVertices();

            // top left corner of player
            if (((playerTopLeftX > verticesObject[topLeftX] && playerTopLeftX < verticesObject[topRightX])
                    || (playerTopLeftX + 0.25f > verticesObject[botLeftX] && playerTopLeftX +0.25f < verticesObject[botRightX]))
                    &&
                    (playerTopLeftY > verticesObject[botLeftY] && playerTopLeftY < verticesObject[topLeftY])
                    || (playerTopLeftY + 0.25f > verticesObject[botRightY] && playerTopLeftY + 0.25f < verticesObject[topRightY]))
            {
                collides = true;
                System.out.println("colides");
            }
        }
        // memory leak ->
        System.out.println(playerTopLeftX + "  " + playerTopLeftY);

        collides = false;
    }


    public static float[] green = {
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f
    };
    public static float[] red = {
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
    };

    public static void doesCollide() {
        /*
        if (checkCollision()) {
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, mainPlayer.getColorsId());

            FloatBuffer cb = BufferUtils.createFloatBuffer(green.length)
                    .put(green)
                    .flip();

            // Send the buffer (positions) to the GPU
            GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cb, GL33.GL_STATIC_DRAW);
            GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 0, 0);
        }
        else {
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, mainPlayer.getColorsId());

            FloatBuffer cb = BufferUtils.createFloatBuffer(red.length)
                    .put(red)
                    .flip();

            // Send the buffer (positions) to the GPU
            GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cb, GL33.GL_STATIC_DRAW);
            GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 0, 0);
        */
    }




}
