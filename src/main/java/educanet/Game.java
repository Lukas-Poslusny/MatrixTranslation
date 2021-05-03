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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Game {

    public static void init(long window) {
        // Setup shaders
        Shaders.initShaders();
        prepareGamefield();
        renderGamefield();

        createPlayer();

    }

    public static void render(long window) {
        createPlayer();
        renderGamefield();
    }

    public static void update(long window) {
        movePlayer(window, Player.getMatrix());
        // TODO collision
    }


    // PLAYER

    public static void createPlayer() {
        Player player = new Player();

        GL33.glUseProgram(Shaders.shaderProgramId); // use this shader to render
        GL33.glBindVertexArray(player.getSquareVaoId());
        GL33.glDrawElements(GL33.GL_TRIANGLES, player.getVertices().length, GL33.GL_UNSIGNED_INT, 0);
    }

    /*
    public static void renderPlayer() {
        // Draw using the glDrawElements function
        GL33.glUseProgram(Shaders.shaderProgramId);
        GL33.glBindVertexArray(Player.getSquareVaoId());
        GL33.glDrawElements(GL33.GL_TRIANGLES, Player.getIndices().length, GL33.GL_UNSIGNED_INT, 0);
    }
     */

    public static void movePlayer(long window, Matrix4f matrix) { // TODO
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) { // Move forward
            matrix = matrix.translate(0, 0.0008f, 0f);
        }
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) { // Left
            matrix = matrix.translate(-0.0008f, 0f, 0f);
        }
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) { // Move backwards
            matrix = matrix.translate(0f, -0.0008f, 0f);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) { // Move right
            matrix = matrix.translate(0.0008f, 0f, 0f);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Q) == GLFW.GLFW_PRESS) { // Rotate left
            matrix = matrix.rotateZ(0.0008f);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS) { // Rotate right
            matrix = matrix.rotateZ(-0.0008f);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS) { // Reset
            matrix = matrix.rotationX(0f).scale(0.25f, 0.25f, 0.25f);
        }

        // TODO: Send to GPU only if position updated
        matrix.get(Player.getMatrixBuffer());
        GL33.glUniformMatrix4fv(Player.getUniformMatrixLocation(), false, Player.getMatrixBuffer());
    }


    // GAMEFIELD

    public static ArrayList<Gamefield> gamefieldObjectArrayList = new ArrayList<>();

    public static String gameField;
    public static int numberOfObjectsGamefield;

    public static void prepareGamefield() { // read file, convert to verticies, add Squares to ArrayList

        String path = "src/main/gameResources/gamefield.txt";
        File level = new File(path);

        if (level.exists() && level.canRead()) //checks if maze file exists and is readable
            gameField = FileUtils.readFile(path);


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

    public static void renderGamefield() {
        for (int i = 0; i < gamefieldObjectArrayList.size(); i++) {
            Gamefield g = gamefieldObjectArrayList.get(i);

            GL33.glUseProgram(Shaders.shaderProgramId); // use this shader to render
            GL33.glBindVertexArray(g.getVaoId());
            GL33.glDrawElements(GL33.GL_TRIANGLES, g.getVertices().length, GL33.GL_UNSIGNED_INT, 0);
        }
    }

}
