package com.artemis.player.render.util;

import android.util.Log;

import static android.opengl.GLES30.GL_COMPILE_STATUS;
import static android.opengl.GLES30.GL_FRAGMENT_SHADER;
import static android.opengl.GLES30.GL_LINK_STATUS;
import static android.opengl.GLES30.GL_VALIDATE_STATUS;
import static android.opengl.GLES30.GL_VERTEX_SHADER;
import static android.opengl.GLES30.glAttachShader;
import static android.opengl.GLES30.glCompileShader;
import static android.opengl.GLES30.glCreateProgram;
import static android.opengl.GLES30.glCreateShader;
import static android.opengl.GLES30.glDeleteProgram;
import static android.opengl.GLES30.glDeleteShader;
import static android.opengl.GLES30.glGetProgramInfoLog;
import static android.opengl.GLES30.glGetProgramiv;
import static android.opengl.GLES30.glGetShaderInfoLog;
import static android.opengl.GLES30.glGetShaderiv;
import static android.opengl.GLES30.glLinkProgram;
import static android.opengl.GLES30.glShaderSource;
import static android.opengl.GLES30.glValidateProgram;

/**
 * Created by xrealm on 2017/9/5.
 */
public class ShaderUtil {

    private static final String TAG = "[ShaderUtil.java]";

    public static int buildProgram(String vertexSourceCode, String fragmentSourceCode) {
        int program;
        //compile
        int vertexShader = compileVertexShader(vertexSourceCode);
        int fragmentShader = compileFragmentShader(fragmentSourceCode);
        //link
        program = linkProgram(vertexShader, fragmentShader);
        validateProgram(program);
        return program;
    }

    private static void validateProgram(int program) {
        glValidateProgram(program);
        int[] validateStatus = new int[1];
        glGetProgramiv(program, GL_VALIDATE_STATUS, validateStatus, 0);
        if (validateStatus[0] == 0) {
            Log.e(TAG, "Results of validating program : " + validateStatus[0]
                    + "\n Log : " + glGetProgramInfoLog(program));
        }
    }

    private static int linkProgram(int vertexShader, int fragmentShader) {
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        int[] linkStatus = new int[1];
        glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            glDeleteProgram(program);
            Log.e(TAG, "Linking of program failed. Reason : \n" + glGetProgramInfoLog(program));
            program = 0;
        }
        return program;
    }

    private static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    private static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderSourceCode) {
        int shaderId = glCreateShader(type);
        String errInfo = "none";
        if (shaderId != 0) {
            glShaderSource(shaderId, shaderSourceCode);
            glCompileShader(shaderId);
            int[] compileStatus = new int[1];
            glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                errInfo = glGetShaderInfoLog(shaderId);
                glDeleteShader(shaderId);
                shaderId = 0;
            }
        }
        if (shaderId == 0) {
            Log.e(TAG, "could not create new shader. Reason : \n" + errInfo);
        }
        return shaderId;
    }
}
