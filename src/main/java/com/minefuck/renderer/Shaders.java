package com.minefuck.renderer;

/**
 * GLSL shader source code for the game.
 */
public class Shaders {

    // ==================== WORLD SHADER ====================

    public static final String WORLD_VERTEX = """
            #version 330 core
            
            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec3 aColor;
            layout (location = 2) in vec3 aNormal;
            
            uniform mat4 projection;
            uniform mat4 view;
            
            out vec3 fragColor;
            out vec3 fragNormal;
            out vec3 fragPos;
            out float fogFactor;
            
            uniform float fogStart;
            uniform float fogEnd;
            uniform vec3 cameraPos;
            
            void main() {
                gl_Position = projection * view * vec4(aPos, 1.0);
                fragColor = aColor;
                fragNormal = aNormal;
                fragPos = aPos;
                
                // Fog calculation
                float dist = length(aPos - cameraPos);
                fogFactor = clamp((fogEnd - dist) / (fogEnd - fogStart), 0.0, 1.0);
            }
            """;

    public static final String WORLD_FRAGMENT = """
            #version 330 core
            
            in vec3 fragColor;
            in vec3 fragNormal;
            in vec3 fragPos;
            in float fogFactor;
            
            uniform vec3 lightDir;
            uniform vec3 skyColor;
            uniform float ambientStrength;
            
            out vec4 FragColor;
            
            void main() {
                // Diffuse lighting
                float diff = max(dot(fragNormal, normalize(lightDir)), 0.0);
                float light = ambientStrength + diff * (1.0 - ambientStrength);
                
                vec3 litColor = fragColor * light;
                
                // Apply fog
                vec3 finalColor = mix(skyColor, litColor, fogFactor);
                
                FragColor = vec4(finalColor, 1.0);
            }
            """;

    // ==================== SKY SHADER ====================

    public static final String SKY_VERTEX = """
            #version 330 core
            
            layout (location = 0) in vec3 aPos;
            
            out vec3 fragPos;
            
            void main() {
                gl_Position = vec4(aPos, 1.0);
                fragPos = aPos;
            }
            """;

    public static final String SKY_FRAGMENT = """
            #version 330 core
            
            in vec3 fragPos;
            
            uniform vec3 topColor;
            uniform vec3 bottomColor;
            
            out vec4 FragColor;
            
            void main() {
                float t = (fragPos.y + 1.0) * 0.5;
                vec3 color = mix(bottomColor, topColor, t);
                FragColor = vec4(color, 1.0);
            }
            """;

    // ==================== HUD SHADER ====================

    public static final String HUD_VERTEX = """
            #version 330 core
            
            layout (location = 0) in vec2 aPos;
            
            uniform mat4 projection;
            
            out vec2 fragPos;
            
            void main() {
                gl_Position = projection * vec4(aPos, 0.0, 1.0);
                fragPos = aPos;
            }
            """;

    public static final String HUD_FRAGMENT = """
            #version 330 core
            
            in vec2 fragPos;
            
            uniform vec3 color;
            uniform float alpha;
            
            out vec4 FragColor;
            
            void main() {
                FragColor = vec4(color, alpha);
            }
            """;

    // ==================== HIGHLIGHT SHADER ====================

    public static final String HIGHLIGHT_VERTEX = """
            #version 330 core
            
            layout (location = 0) in vec3 aPos;
            
            uniform mat4 projection;
            uniform mat4 view;
            uniform vec3 blockPos;
            
            void main() {
                vec3 pos = aPos * 1.002 + blockPos - vec3(0.001); // slightly bigger to avoid z-fighting
                gl_Position = projection * view * vec4(pos, 1.0);
            }
            """;

    public static final String HIGHLIGHT_FRAGMENT = """
            #version 330 core
            
            out vec4 FragColor;
            
            void main() {
                FragColor = vec4(0.0, 0.0, 0.0, 0.3);
            }
            """;
}
