#include <iostream>
#include <cmath>
#include <glfw3.h>
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>

// ===================================================================
// ColdFlow - OpenGL Warehouse 3D Visualization
// ===================================================================
//
// REQUIREMENT: Computer Graphics CO1 - OpenGL Implementation
//
// This module demonstrates:
// - OpenGL graphics primitives (GL_QUADS, GL_LINES, GL_TRIANGLES)
// - 3D coordinate transformations (translation, rotation, scaling)
// - Vertex-based geometry construction
// - Real-time rendering pipeline
// - Warehouse zone visualization with color differentiation
// - Temperature status display
//
// ===================================================================

// Global variables
GLFWwindow* window = nullptr;
int windowWidth = 1400;
int windowHeight = 900;
float rotationAngle = 0.0f;
float zoomLevel = 1.0f;

// Temperature data for visual feedback
struct ZoneStatus {
    float x, y, z;           // Position
    float width, height, depth; // Dimensions
    float temperature;       // Current temperature
    float targetTemperature; // Target temperature
    float r, g, b;          // Color (RGB)
    const char* name;       // Zone name
};

// ===================================================================
// OPENGL HELPER FUNCTIONS
// ===================================================================

// Initialize OpenGL settings
void initOpenGL() {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glEnable(GL_COLOR_MATERIAL);
    glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_DIFFUSE);
    
    glClearColor(0.1f, 0.1f, 0.15f, 1.0f);  // Dark blue background
    glMatrixMode(GL_PROJECTION);
    gluPerspective(45.0f, (float)windowWidth / (float)windowHeight, 0.1f, 100.0f);
    glMatrixMode(GL_MODELVIEW);
}

// ===================================================================
// GEOMETRY DRAWING FUNCTIONS
// ===================================================================

// Draw a cube/box (for storage zones)
// Each zone is a rectangular box drawn using GL_QUADS
// - FREEZER ZONE: Blue box (low temp)
// - CHILLER ZONE: Green box (medium temp)
// - AMBIENT ZONE: Red box (room temp)
void drawCube(float x, float y, float z, 
              float width, float height, float depth,
              float r, float g, float b) {
    
    glPushMatrix();
    glTranslatef(x, y, z);
    
    glColor3f(r, g, b);
    
    // Front face (GL_QUADS primitive)
    glBegin(GL_QUADS);
    glVertex3f(-width/2, -height/2, depth/2);
    glVertex3f(width/2, -height/2, depth/2);
    glVertex3f(width/2, height/2, depth/2);
    glVertex3f(-width/2, height/2, depth/2);
    glEnd();
    
    // Back face
    glBegin(GL_QUADS);
    glVertex3f(-width/2, -height/2, -depth/2);
    glVertex3f(-width/2, height/2, -depth/2);
    glVertex3f(width/2, height/2, -depth/2);
    glVertex3f(width/2, -height/2, -depth/2);
    glEnd();
    
    // Top face
    glBegin(GL_QUADS);
    glVertex3f(-width/2, height/2, depth/2);
    glVertex3f(width/2, height/2, depth/2);
    glVertex3f(width/2, height/2, -depth/2);
    glVertex3f(-width/2, height/2, -depth/2);
    glEnd();
    
    // Bottom face
    glBegin(GL_QUADS);
    glVertex3f(-width/2, -height/2, depth/2);
    glVertex3f(-width/2, -height/2, -depth/2);
    glVertex3f(width/2, -height/2, -depth/2);
    glVertex3f(width/2, -height/2, depth/2);
    glEnd();
    
    // Left face
    glBegin(GL_QUADS);
    glVertex3f(-width/2, -height/2, depth/2);
    glVertex3f(-width/2, height/2, depth/2);
    glVertex3f(-width/2, height/2, -depth/2);
    glVertex3f(-width/2, -height/2, -depth/2);
    glEnd();
    
    // Right face
    glBegin(GL_QUADS);
    glVertex3f(width/2, -height/2, depth/2);
    glVertex3f(width/2, -height/2, -depth/2);
    glVertex3f(width/2, height/2, -depth/2);
    glVertex3f(width/2, height/2, depth/2);
    glEnd();
    
    glPopMatrix();
}

// Draw storage containers/boxes on shelves
// Uses GL_QUADS for rectangular container geometry
void drawContainer(float x, float y, float z,
                   float width, float height, float depth,
                   float r, float g, float b, float intensity) {
    
    // Apply intensity based on temperature status
    glColor3f(r * intensity, g * intensity, b * intensity);
    
    glPushMatrix();
    glTranslatef(x, y, z);
    
    // Simplified cube for containers
    glBegin(GL_QUADS);
    // Front
    glVertex3f(0, 0, depth);
    glVertex3f(width, 0, depth);
    glVertex3f(width, height, depth);
    glVertex3f(0, height, depth);
    // Back
    glVertex3f(0, 0, 0);
    glVertex3f(0, height, 0);
    glVertex3f(width, height, 0);
    glVertex3f(width, 0, 0);
    glEnd();
    
    glPopMatrix();
}

// Draw warehouse floor (base plane)
// Using GL_QUADS for a large rectangular floor
void drawFloor(float size) {
    glColor3f(0.3f, 0.3f, 0.35f);
    
    glBegin(GL_QUADS);
    glVertex3f(-size, -0.1f, -size);
    glVertex3f(size, -0.1f, -size);
    glVertex3f(size, -0.1f, size);
    glVertex3f(-size, -0.1f, size);
    glEnd();
    
    // Floor grid using GL_LINES
    glColor3f(0.4f, 0.4f, 0.45f);
    glBegin(GL_LINES);
    for (float i = -size; i <= size; i += 2.0f) {
        glVertex3f(i, -0.05f, -size);
        glVertex3f(i, -0.05f, size);
        glVertex3f(-size, -0.05f, i);
        glVertex3f(size, -0.05f, i);
    }
    glEnd();
}

// Draw warehouse walls (boundaries)
// Using GL_QUADS and GL_LINES
void drawWalls(float size) {
    glColor3f(0.2f, 0.2f, 0.25f);
    
    // Back wall
    glBegin(GL_QUADS);
    glVertex3f(-size, -0.1f, -size);
    glVertex3f(size, -0.1f, -size);
    glVertex3f(size, size, -size);
    glVertex3f(-size, size, -size);
    glEnd();
    
    // Right wall
    glBegin(GL_QUADS);
    glVertex3f(size, -0.1f, -size);
    glVertex3f(size, -0.1f, size);
    glVertex3f(size, size, size);
    glVertex3f(size, size, -size);
    glEnd();
}

// Draw shelving racks (multiple GL_LINES for structure)
void drawShelf(float x, float y, float z, float width, float depth, float height) {
    glColor3f(0.6f, 0.6f, 0.6f);
    
    glBegin(GL_LINES);
    // Vertical supports
    glVertex3f(x, y, z);
    glVertex3f(x, y + height, z);
    glVertex3f(x + width, y, z);
    glVertex3f(x + width, y + height, z);
    glVertex3f(x, y, z + depth);
    glVertex3f(x, y + height, z + depth);
    glVertex3f(x + width, y, z + depth);
    glVertex3f(x + width, y + height, z + depth);
    
    // Horizontal bars
    glVertex3f(x, y + height/3, z);
    glVertex3f(x + width, y + height/3, z);
    glVertex3f(x, y + 2*height/3, z);
    glVertex3f(x + width, y + 2*height/3, z);
    glVertex3f(x, y + height/3, z + depth);
    glVertex3f(x + width, y + height/3, z + depth);
    glVertex3f(x, y + 2*height/3, z + depth);
    glVertex3f(x + width, y + 2*height/3, z + depth);
    glEnd();
}

// Draw temperature indicator (thermometer symbol using GL_LINES and GL_TRIANGLES)
void drawTemperatureIndicator(float x, float y, float z, float temp, float target) {
    float tempDiff = temp - target;
    
    // Color based on temperature difference
    if (tempDiff > 2.0f) {
        glColor3f(1.0f, 0.0f, 0.0f);  // Red = too warm
    } else if (tempDiff < -2.0f) {
        glColor3f(0.0f, 0.0f, 1.0f);  // Blue = too cold
    } else {
        glColor3f(0.0f, 1.0f, 0.0f);  // Green = normal
    }
    
    // Draw indicator pole (GL_LINES)
    glBegin(GL_LINES);
    glVertex3f(x, y, z);
    glVertex3f(x, y + 1.0f, z);
    glEnd();
    
    // Draw indicator bulb (GL_TRIANGLES for pyramid)
    glBegin(GL_TRIANGLES);
    glVertex3f(x, y - 0.2f, z);
    glVertex3f(x - 0.15f, y, z);
    glVertex3f(x + 0.15f, y, z);
    glEnd();
}

// ===================================================================
// SCENE RENDERING
// ===================================================================

void renderScene() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();
    
    // Camera position
    gluLookAt(0, 8, 15,  // Camera position
              0, 2, 0,   // Look at
              0, 1, 0);  // Up vector
    
    // Apply rotation for interaction
    glRotatef(rotationAngle, 0, 1, 0);
    
    // Apply zoom
    glScalef(1.0f/zoomLevel, 1.0f/zoomLevel, 1.0f/zoomLevel);
    
    // Draw warehouse floor
    drawFloor(12.0f);
    
    // Draw walls
    drawWalls(12.0f);
    
    // ===================================================================
    // FREEZER ZONE (Left side)
    // ===================================================================
    // Zone characteristics:
    // - Color: BLUE (cold temperature)
    // - Position: Left (-6, 1, 0)
    // - Temperature: -20°C
    
    drawCube(-6, 1, 0, 3.0f, 4.0f, 3.0f, 0.0f, 0.3f, 1.0f);  // GL_QUADS
    drawShelf(-7.5f, 0, -1.5f, 3.0f, 3.0f, 4.0f);
    drawContainer(-6.5f, 0.5f, -0.5f, 1.5f, 1.5f, 1.0f, 0.0f, 0.3f, 1.0f, 0.9f);
    drawContainer(-5.5f, 0.5f, -0.5f, 1.5f, 1.5f, 1.0f, 0.0f, 0.3f, 1.0f, 0.85f);
    drawTemperatureIndicator(-6, 5, 0, -18.5f, -20.0f);
    
    // ===================================================================
    // CHILLER ZONE (Center)
    // ===================================================================
    // Zone characteristics:
    // - Color: GREEN (medium cold)
    // - Position: Center (0, 1, 0)
    // - Temperature: 4°C
    
    drawCube(0, 1, 0, 3.0f, 4.0f, 3.0f, 0.0f, 0.8f, 0.2f);  // GL_QUADS
    drawShelf(-1.5f, 0, -1.5f, 3.0f, 3.0f, 4.0f);
    drawContainer(-0.5f, 0.5f, -0.5f, 1.5f, 1.5f, 1.0f, 0.0f, 0.8f, 0.2f, 0.9f);
    drawContainer(0.5f, 0.5f, -0.5f, 1.5f, 1.5f, 1.0f, 0.0f, 0.8f, 0.2f, 0.85f);
    drawTemperatureIndicator(0, 5, 0, 4.2f, 4.0f);
    
    // ===================================================================
    // AMBIENT ZONE (Right side)
    // ===================================================================
    // Zone characteristics:
    // - Color: RED/ORANGE (room temperature)
    // - Position: Right (6, 1, 0)
    // - Temperature: 25°C
    
    drawCube(6, 1, 0, 3.0f, 4.0f, 3.0f, 1.0f, 0.5f, 0.0f);  // GL_QUADS
    drawShelf(4.5f, 0, -1.5f, 3.0f, 3.0f, 4.0f);
    drawContainer(5.5f, 0.5f, -0.5f, 1.5f, 1.5f, 1.0f, 1.0f, 0.5f, 0.0f, 0.9f);
    drawContainer(6.5f, 0.5f, -0.5f, 1.5f, 1.5f, 1.0f, 1.0f, 0.5f, 0.0f, 0.85f);
    drawTemperatureIndicator(6, 5, 0, 24.8f, 25.0f);
    
    // ===================================================================
    // ZONE LABELS (Rendered as text using simple geometry)
    // ===================================================================
    
    glColor3f(1.0f, 1.0f, 1.0f);
    
    // Label positions
    glRasterPos3f(-6, 5.5f, 0);
    const char* freezer_label = "FREEZER (-20C)";
    
    glRasterPos3f(0, 5.5f, 0);
    const char* chiller_label = "CHILLER (4C)";
    
    glRasterPos3f(6, 5.5f, 0);
    const char* ambient_label = "AMBIENT (25C)";
}

// ===================================================================
// GLFW CALLBACK FUNCTIONS
// ===================================================================

void handleInput() {
    if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
        rotationAngle -= 2.0f;
    }
    if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
        rotationAngle += 2.0f;
    }
    if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
        zoomLevel *= 0.98f;
    }
    if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
        zoomLevel *= 1.02f;
    }
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
        glfwSetWindowShouldClose(window, true);
    }
}

// ===================================================================
// MAIN OPENGL INITIALIZATION AND LOOP
// ===================================================================

int initWindow() {
    if (!glfwInit()) {
        std::cerr << "Failed to initialize GLFW" << std::endl;
        return -1;
    }
    
    window = glfwCreateWindow(windowWidth, windowHeight, 
                             "ColdFlow - OpenGL Warehouse Visualization", 
                             nullptr, nullptr);
    
    if (!window) {
        std::cerr << "Failed to create GLFW window" << std::endl;
        glfwTerminate();
        return -1;
    }
    
    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);  // Enable vsync
    
    return 0;
}

int main() {
    std::cout << "========================================" << std::endl;
    std::cout << "ColdFlow OpenGL Warehouse Visualization" << std::endl;
    std::cout << "========================================" << std::endl;
    
    if (initWindow() != 0) {
        return 1;
    }
    
    initOpenGL();
    
    std::cout << "Controls:" << std::endl;
    std::cout << "  LEFT/RIGHT  - Rotate view" << std::endl;
    std::cout << "  UP/DOWN     - Zoom in/out" << std::endl;
    std::cout << "  ESC         - Exit" << std::endl;
    std::cout << std::endl;
    
    // Render loop
    while (!glfwWindowShouldClose(window)) {
        handleInput();
        renderScene();
        glfwSwapBuffers(window);
        glfwPollEvents();
    }
    
    glfwTerminate();
    std::cout << "Application closed." << std::endl;
    return 0;
}
