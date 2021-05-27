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

    //Colors
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

    static FloatBuffer cb = BufferUtils.createFloatBuffer(12).put(green).flip();

    // Player
    public static Player mainPlayer;
    public static float playerTopLeftX = -0.125f;
    public static float playerTopLeftY = 0.125f;

    // Gamefield
    public static ArrayList<Gamefield> gamefieldObjectArrayList = new ArrayList<>(); // list that holds all background objects

    //
    static int topLeftX = 9;
    static int topLeftY = 10;
    static int topRightX = 0;
    static int topRightY = 1;
    static int botLeftX = 6;
    static int botLeftY = 7;
    static int botRightX = 3;
    static int botRightY = 4;


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
        movePlayer(window, Player.getMatrix()); // change players position with WASD
        doesCollide(); // check whether player collides with terrain
    }


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

    public static void renderGamefield() {
        Gamefield.matrix.get(Gamefield.matrixBuffer);
        int location = GL33.glGetUniformLocation(Shaders.shaderProgramId, "matrix");
        GL33.glUniformMatrix4fv(location, false, Gamefield.matrixBuffer);

        for (Gamefield g : gamefieldObjectArrayList) {
            GL33.glUseProgram(Shaders.shaderProgramId); // use this shader to render
            GL33.glBindVertexArray(g.getVaoId());
            GL33.glDrawElements(GL33.GL_TRIANGLES, g.getVertices().length, GL33.GL_UNSIGNED_INT, 0);
        }
    }

    public static void prepareGamefield() { // read file, convert to verticies, add Squares to ArrayList
        String gameField = "";
        int numberOfObjectsGamefield = 0;

        String path = "src/main/gameResources/gamefield.txt";
        File level = new File(path);

        if (level.exists() && level.canRead()) { //checks if maze file exists and is readable
            gameField = FileUtils.readFile(path);
        }
        Matcher m = Pattern.compile("\r\n|\r|\n").matcher(gameField); // using matcher to not fill up gc with useless strings from .split();
        while (m.find()) { // while matches has next line, increment number
            numberOfObjectsGamefield++;
        }

        String[] objs = gameField.split("\n"); // split into strings by new line

        for (int i = 0; i < numberOfObjectsGamefield; i++) { // for reach object
            String[] objAtrribs = objs[i].split(";");
            Gamefield g = new Gamefield();
            float[] newVerticies = { // converts coordinates to verticies
                    Float.parseFloat(objAtrribs[0]) + Float.parseFloat(objAtrribs[2]), Float.parseFloat(objAtrribs[1]),                                   0, // top right
                    Float.parseFloat(objAtrribs[0]) + Float.parseFloat(objAtrribs[2]), Float.parseFloat(objAtrribs[1]) - Float.parseFloat(objAtrribs[2]), 0, // bottom right
                    Float.parseFloat(objAtrribs[0]),                                   Float.parseFloat(objAtrribs[1]) - Float.parseFloat(objAtrribs[2]), 0, // bottom left
                    Float.parseFloat(objAtrribs[0]),                                   Float.parseFloat(objAtrribs[1]),                                   0, // top left
            };
            g.setVertices(newVerticies); // assign verticies to temporary instance of Gamefield
            gamefieldObjectArrayList.add(g); // add
        }
    }

    public static boolean checkCollision() {
        for (int i = 0; i < gamefieldObjectArrayList.size(); i++) {
            // for all 4 corner of player if x is in interval of (x left < corner < x right of object)
            Gamefield g = gamefieldObjectArrayList.get(i);

            float[] verticesObject = g.getVertices();


            // top left corner of player
            if (((playerTopLeftX > verticesObject[topLeftX] && playerTopLeftX < verticesObject[topRightX]) // check upper x axis
                    || (playerTopLeftX + 0.25f > verticesObject[botLeftX] && playerTopLeftX +0.25f < verticesObject[botRightX])) // check bottom x axis
                    &&
                    ((playerTopLeftY > verticesObject[botLeftY] && playerTopLeftY < verticesObject[topLeftY]) // check left-side y axis
                    || (playerTopLeftY - 0.25f > verticesObject[botRightY] && playerTopLeftY - 0.25f < verticesObject[topRightY]))) // check right-side y axis
            {
                return true;
            }
        }
        return false;
    }

    public static void doesCollide() {
        GL33.glBindVertexArray(mainPlayer.getSquareVaoId());

        if (checkCollision()) {
            cb.clear()
                    .put(green)
                    .flip();

            // Send the buffer (positions) to the GPU
        } else {
            cb.clear()
                    .put(red)
                    .flip();

            // Send the buffer (positions) to the GPU
        }
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 0, 0);
    }
}
