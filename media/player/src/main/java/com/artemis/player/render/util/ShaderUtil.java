package com.artemis.player.render.util;

import android.opengl.GLES30;
import android.util.Log;

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
        GLES30.glValidateProgram(program);
        int[] validateStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_VALIDATE_STATUS, validateStatus, 0);
        if (validateStatus[0] == 0) {
            Log.e(TAG, "Results of validating program : " + validateStatus[0]
                    + "\n Log : " + GLES30.glGetProgramInfoLog(program));
        }
    }

    private static int linkProgram(int vertexShader, int fragmentShader) {
        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            GLES30.glDeleteProgram(program);
            Log.e(TAG, "Linking of program failed. Reason : \n" + GLES30.glGetProgramInfoLog(program));
            program = 0;
        }
        return program;
    }

    private static int compileVertexShader(String shaderCode) {
        return compileShader(GLES30.GL_VERTEX_SHADER, shaderCode);
    }

    private static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES30.GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderSourceCode) {
        int shaderId = GLES30.glCreateShader(type);
        String errInfo = "none";
        if (shaderId != 0) {
            GLES30.glShaderSource(shaderId, shaderSourceCode);
            GLES30.glCompileShader(shaderId);
            int[] compileStatus = new int[1];
            GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                errInfo = GLES30.glGetShaderInfoLog(shaderId);
                GLES30.glDeleteShader(shaderId);
                shaderId = 0;
            }
        }
        if (shaderId == 0) {
            Log.e(TAG, "could not create new shader. Reason : \n" + errInfo);
        }
        return shaderId;
    }
}
