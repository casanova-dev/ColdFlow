#include <iostream>
#include <algorithm>
#include <cmath>
#include <cstdio>
#include <cstring>
#include <GLFW/glfw3.h>
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
float cameraElevation = 22.0f;
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

const ZoneStatus zones[] = {
    {-6.0f, 1.0f, 0.0f, 3.0f, 4.0f, 3.0f, -18.5f, -20.0f, 0.0f, 0.3f, 1.0f, "FREEZER"},
    { 0.0f, 1.0f, 0.0f, 3.0f, 4.0f, 3.0f,   4.2f,   4.0f, 0.0f, 0.8f, 0.2f, "CHILLER"},
    { 6.0f, 1.0f, 0.0f, 3.0f, 4.0f, 3.0f,  24.8f,  25.0f, 1.0f, 0.5f, 0.0f, "AMBIENT"}
};

const float MIN_ZOOM = 0.5f;
const float MAX_ZOOM = 2.5f;
const float MIN_ELEVATION = 5.0f;
const float MAX_ELEVATION = 80.0f;

// ===================================================================
// OPENGL HELPER FUNCTIONS
// ===================================================================

void updateProjection(int width, int height) {
    if (width <= 0 || height <= 0) {
        return;
    }

    windowWidth = width;
    windowHeight = height;
    glViewport(0, 0, width, height);
    glMatrixMode(GL_PROJECTION);

    const glm::mat4 projection = glm::perspective(
        glm::radians(45.0f),
        static_cast<float>(width) / static_cast<float>(height),
        0.1f,
        100.0f
    );
    glLoadMatrixf(glm::value_ptr(projection));
    glMatrixMode(GL_MODELVIEW);
}

void handleFramebufferResize(GLFWwindow*, int width, int height) {
    updateProjection(width, height);
}

// Initialize OpenGL settings
void initOpenGL() {
    glEnable(GL_DEPTH_TEST);
    glDisable(GL_LIGHTING);
    glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
    updateProjection(windowWidth, windowHeight);
}

// ===================================================================
// GEOMETRY DRAWING FUNCTIONS
// ===================================================================

// Draw zone boundaries as an open wireframe so the contents remain visible.
void drawCube(float x, float y, float z,
              float width, float height, float depth,
              float r, float g, float b) {

    glPushMatrix();
    glTranslatef(x, y, z);

    glColor3f(r, g, b);
    const float x1 = -width / 2.0f;
    const float x2 = width / 2.0f;
    const float y1 = -height / 2.0f;
    const float y2 = height / 2.0f;
    const float z1 = -depth / 2.0f;
    const float z2 = depth / 2.0f;

    glBegin(GL_LINES);
    // Front and back rectangles
    glVertex3f(x1, y1, z2); glVertex3f(x2, y1, z2);
    glVertex3f(x2, y1, z2); glVertex3f(x2, y2, z2);
    glVertex3f(x2, y2, z2); glVertex3f(x1, y2, z2);
    glVertex3f(x1, y2, z2); glVertex3f(x1, y1, z2);
    glVertex3f(x1, y1, z1); glVertex3f(x2, y1, z1);
    glVertex3f(x2, y1, z1); glVertex3f(x2, y2, z1);
    glVertex3f(x2, y2, z1); glVertex3f(x1, y2, z1);
    glVertex3f(x1, y2, z1); glVertex3f(x1, y1, z1);
    // Connect corresponding corners
    glVertex3f(x1, y1, z1); glVertex3f(x1, y1, z2);
    glVertex3f(x2, y1, z1); glVertex3f(x2, y1, z2);
    glVertex3f(x1, y2, z1); glVertex3f(x1, y2, z2);
    glVertex3f(x2, y2, z1); glVertex3f(x2, y2, z2);
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

    glBegin(GL_QUADS);
    // Front, back, top, bottom, left, and right faces
    glVertex3f(0, 0, depth); glVertex3f(width, 0, depth);
    glVertex3f(width, height, depth); glVertex3f(0, height, depth);
    glVertex3f(width, 0, 0); glVertex3f(0, 0, 0);
    glVertex3f(0, height, 0); glVertex3f(width, height, 0);
    glVertex3f(0, height, depth); glVertex3f(width, height, depth);
    glVertex3f(width, height, 0); glVertex3f(0, height, 0);
    glVertex3f(0, 0, 0); glVertex3f(width, 0, 0);
    glVertex3f(width, 0, depth); glVertex3f(0, 0, depth);
    glVertex3f(0, 0, 0); glVertex3f(0, 0, depth);
    glVertex3f(0, height, depth); glVertex3f(0, height, 0);
    glVertex3f(width, 0, depth); glVertex3f(width, 0, 0);
    glVertex3f(width, height, 0); glVertex3f(width, height, depth);
    glEnd();

    glColor3f(0.08f, 0.1f, 0.14f);
    glBegin(GL_QUADS);
    glVertex3f(width * 0.12f, height * 0.58f, depth + 0.01f);
    glVertex3f(width * 0.88f, height * 0.58f, depth + 0.01f);
    glVertex3f(width * 0.88f, height * 0.88f, depth + 0.01f);
    glVertex3f(width * 0.12f, height * 0.88f, depth + 0.01f);
    glEnd();

    glColor3f(0.75f, 0.85f, 0.9f);
    glBegin(GL_LINES);
    for (int stripe = 0; stripe < 4; ++stripe) {
        const float stripeX = width * (0.2f + stripe * 0.08f);
        glVertex3f(stripeX, height * 0.63f, depth + 0.02f);
        glVertex3f(stripeX, height * 0.82f, depth + 0.02f);
    }
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

    glColor3f(0.95f, 0.65f, 0.12f);
    glBegin(GL_LINES);
    for (float x = -10.0f; x < 10.0f; x += 1.5f) {
        glVertex3f(x, -0.035f, 4.5f);
        glVertex3f(x + 0.75f, -0.035f, 4.5f);
    }
    glVertex3f(-10.0f, -0.035f, 4.2f);
    glVertex3f(10.0f, -0.035f, 4.2f);
    glVertex3f(-10.0f, -0.035f, 4.8f);
    glVertex3f(10.0f, -0.035f, 4.8f);
    glEnd();
}

void drawZonePlatform(const ZoneStatus& zone) {
    const float halfWidth = zone.width / 2.0f + 0.35f;
    const float halfDepth = zone.depth / 2.0f + 0.35f;
    const float front = zone.z + halfDepth;
    const float back = zone.z - halfDepth;
    const float left = zone.x - halfWidth;
    const float right = zone.x + halfWidth;

    glColor3f(zone.r * 0.16f, zone.g * 0.16f, zone.b * 0.16f);
    glBegin(GL_QUADS);
    glVertex3f(left, -0.02f, back);
    glVertex3f(right, -0.02f, back);
    glVertex3f(right, -0.02f, front);
    glVertex3f(left, -0.02f, front);
    glVertex3f(left, -0.1f, front); glVertex3f(right, -0.1f, front);
    glVertex3f(right, -0.02f, front); glVertex3f(left, -0.02f, front);
    glVertex3f(right, -0.1f, back); glVertex3f(left, -0.1f, back);
    glVertex3f(left, -0.02f, back); glVertex3f(right, -0.02f, back);
    glVertex3f(left, -0.1f, back); glVertex3f(left, -0.1f, front);
    glVertex3f(left, -0.02f, front); glVertex3f(left, -0.02f, back);
    glVertex3f(right, -0.1f, front); glVertex3f(right, -0.1f, back);
    glVertex3f(right, -0.02f, back); glVertex3f(right, -0.02f, front);
    glEnd();

    glColor3f(zone.r, zone.g, zone.b);
    glBegin(GL_LINE_LOOP);
    glVertex3f(left, -0.015f, back);
    glVertex3f(right, -0.015f, back);
    glVertex3f(right, -0.015f, front);
    glVertex3f(left, -0.015f, front);
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
void drawShelf(float x, float y, float z,
               float width, float depth, float height) {

    glColor3f(0.24f, 0.29f, 0.35f);
    glBegin(GL_QUADS);
    for (int level = 0; level < 3; ++level) {
        const float shelfY = y + height * (level + 1) / 4.0f;
        glVertex3f(x, shelfY, z);
        glVertex3f(x + width, shelfY, z);
        glVertex3f(x + width, shelfY, z + depth);
        glVertex3f(x, shelfY, z + depth);
    }
    glEnd();

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

    // Diagonal braces reinforce the rack frame.
    glVertex3f(x, y, z + depth);
    glVertex3f(x + width, y + height / 3.0f, z + depth);
    glVertex3f(x + width, y, z + depth);
    glVertex3f(x, y + height / 3.0f, z + depth);

    glEnd();
}

// Draw temperature indicator (thermometer symbol using GL_LINES and GL_TRIANGLES)
void drawTemperatureIndicator(float x, float y, float z,
                              float temp, float target) {

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

const unsigned char* glyphRows(char character) {
    static const unsigned char font[36][7] = {
        {14,17,17,31,17,17,17}, {30,17,17,30,17,17,30}, {14,17,16,16,16,17,14},
        {30,17,17,17,17,17,30}, {31,16,16,30,16,16,31}, {31,16,16,30,16,16,16},
        {14,17,16,23,17,17,15}, {17,17,17,31,17,17,17}, {14,4,4,4,4,4,14},
        {7,2,2,2,18,18,12}, {17,18,20,24,20,18,17}, {16,16,16,16,16,16,31},
        {17,27,21,21,17,17,17}, {17,25,25,21,19,19,17}, {14,17,17,17,17,17,14},
        {30,17,17,30,16,16,16}, {14,17,17,17,21,18,13}, {30,17,17,30,20,18,17},
        {15,16,16,14,1,1,30}, {31,4,4,4,4,4,4}, {17,17,17,17,17,17,14},
        {17,17,17,17,17,10,4}, {17,17,17,21,21,21,10}, {17,17,10,4,10,17,17},
        {17,17,10,4,4,4,4}, {31,1,2,4,8,16,31},
        {14,17,19,21,25,17,14}, {4,12,4,4,4,4,14}, {14,17,1,2,4,8,31},
        {30,1,1,14,1,1,30}, {2,6,10,18,31,2,2}, {31,16,16,30,1,1,30},
        {14,16,16,30,17,17,14}, {31,1,2,4,8,8,8}, {14,17,17,14,17,17,14},
        {14,17,17,15,1,1,14}
    };
    static const unsigned char blank[7] = {0,0,0,0,0,0,0};
    static const unsigned char period[7] = {0,0,0,0,0,6,6};
    static const unsigned char hyphen[7] = {0,0,0,31,0,0,0};

    if (character >= 'A' && character <= 'Z') {
        return font[character - 'A'];
    }
    if (character >= '0' && character <= '9') {
        return font[26 + character - '0'];
    }
    if (character == '.') {
        return period;
    }
    if (character == '-') {
        return hyphen;
    }
    return blank;
}

void drawText(const char* text, float centerX, float y, float z, float scale) {
    const std::size_t length = std::strlen(text);
    const float startX = centerX - static_cast<float>(length) * 6.0f * scale / 2.0f;
    glColor3f(1.0f, 1.0f, 1.0f);
    glBegin(GL_QUADS);

    for (std::size_t index = 0; index < length; ++index) {
        const unsigned char* rows = glyphRows(text[index]);
        for (int row = 0; row < 7; ++row) {
            for (int column = 0; column < 5; ++column) {
                if ((rows[row] & (1u << (4 - column))) == 0) {
                    continue;
                }

                const float left = startX + (static_cast<float>(index) * 6.0f + column) * scale;
                const float top = y - static_cast<float>(row) * scale;
                const float pixel = scale * 0.8f;
                glVertex3f(left, top, z);
                glVertex3f(left + pixel, top, z);
                glVertex3f(left + pixel, top - pixel, z);
                glVertex3f(left, top - pixel, z);
            }
        }
    }

    glEnd();
}

void drawZoneSign(const ZoneStatus& zone) {
    const float signZ = zone.z + zone.depth / 2.0f + 0.06f;
    const float signY = zone.y + 1.35f;
    const float signWidth = 2.75f;
    const float signHeight = 1.0f;
    const float statusDifference = std::fabs(zone.temperature - zone.targetTemperature);
    const bool inRange = statusDifference <= 2.0f;

    glColor3f(0.055f, 0.075f, 0.11f);
    glBegin(GL_QUADS);
    glVertex3f(zone.x - signWidth / 2.0f, signY - signHeight / 2.0f, signZ);
    glVertex3f(zone.x + signWidth / 2.0f, signY - signHeight / 2.0f, signZ);
    glVertex3f(zone.x + signWidth / 2.0f, signY + signHeight / 2.0f, signZ);
    glVertex3f(zone.x - signWidth / 2.0f, signY + signHeight / 2.0f, signZ);
    glEnd();

    glColor3f(zone.r, zone.g, zone.b);
    glBegin(GL_LINE_LOOP);
    glVertex3f(zone.x - signWidth / 2.0f, signY - signHeight / 2.0f, signZ + 0.01f);
    glVertex3f(zone.x + signWidth / 2.0f, signY - signHeight / 2.0f, signZ + 0.01f);
    glVertex3f(zone.x + signWidth / 2.0f, signY + signHeight / 2.0f, signZ + 0.01f);
    glVertex3f(zone.x - signWidth / 2.0f, signY + signHeight / 2.0f, signZ + 0.01f);
    glEnd();

    char label[64];
    std::snprintf(label, sizeof(label), "%s %.1fC", zone.name, zone.temperature);
    drawText(label, zone.x, signY + 0.14f, signZ + 0.02f, 0.025f);
    std::snprintf(label, sizeof(label), "TARGET %.1fC", zone.targetTemperature);
    drawText(label, zone.x, signY - 0.2f, signZ + 0.02f, 0.02f);

    glColor3f(inRange ? 0.2f : 1.0f, inRange ? 0.95f : 0.18f, 0.25f);
    glBegin(GL_TRIANGLES);
    glVertex3f(zone.x + signWidth / 2.0f - 0.18f, signY + 0.28f, signZ + 0.02f);
    glVertex3f(zone.x + signWidth / 2.0f - 0.32f, signY + 0.04f, signZ + 0.02f);
    glVertex3f(zone.x + signWidth / 2.0f - 0.04f, signY + 0.04f, signZ + 0.02f);
    glEnd();
}

// ===================================================================
// SCENE RENDERING
// ===================================================================

void renderScene() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();

    const float cameraDistance = 16.2f;
    const float elevationRadians = glm::radians(cameraElevation);
    const glm::vec3 lookAtPoint(0.0f, 2.0f, 0.0f);
    const glm::vec3 cameraPosition(
        0.0f,
        lookAtPoint.y + std::sin(elevationRadians) * cameraDistance,
        std::cos(elevationRadians) * cameraDistance
    );

    glm::mat4 view = glm::lookAt(
        cameraPosition,
        lookAtPoint,
        glm::vec3(0.0f, 1.0f, 0.0f)
    );

    glLoadMatrixf(glm::value_ptr(view));

    // Apply rotation for interaction
    glRotatef(rotationAngle, 0, 1, 0);

    // Apply zoom
    glScalef(1.0f/zoomLevel, 1.0f/zoomLevel, 1.0f/zoomLevel);

    // Draw warehouse floor
    drawFloor(12.0f);

    // Draw walls
    drawWalls(12.0f);

    for (const ZoneStatus& zone : zones) {
        drawZonePlatform(zone);
        drawCube(zone.x, zone.y, zone.z, zone.width, zone.height, zone.depth,
                 zone.r, zone.g, zone.b);
        drawShelf(zone.x - zone.width / 2.0f, 0.0f, zone.z - 1.5f,
                  zone.width, 3.0f, zone.height);

        for (int level = 0; level < 3; ++level) {
            const float containerY = 0.18f + level * 1.0f;
            drawContainer(zone.x - 1.35f, containerY, zone.z - 0.35f,
                          1.2f, 0.82f, 1.1f, zone.r, zone.g, zone.b, 0.9f);
            drawContainer(zone.x + 0.15f, containerY, zone.z - 0.35f,
                          1.2f, 0.82f, 1.1f, zone.r, zone.g, zone.b, 0.78f);
        }

        const float indicatorY = zone.y + zone.height / 2.0f + 1.0f;
        drawTemperatureIndicator(zone.x, indicatorY, zone.z,
                                 zone.temperature, zone.targetTemperature);
        drawZoneSign(zone);
    }
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

    if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
        cameraElevation = std::min(cameraElevation + 0.8f, MAX_ELEVATION);
    }

    if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
        cameraElevation = std::max(cameraElevation - 0.8f, MIN_ELEVATION);
    }

    if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
        zoomLevel *= 0.98f;
    }

    if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
        zoomLevel *= 1.02f;
    }
    zoomLevel = std::max(MIN_ZOOM, std::min(zoomLevel, MAX_ZOOM));

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

    window = glfwCreateWindow(
        windowWidth,
        windowHeight,
        "ColdFlow - OpenGL Warehouse Visualization",
        nullptr,
        nullptr
    );

    if (!window) {
        std::cerr << "Failed to create GLFW window" << std::endl;
        glfwTerminate();
        return -1;
    }

    glfwMakeContextCurrent(window);
    glfwSetFramebufferSizeCallback(window, handleFramebufferResize);
    glfwGetFramebufferSize(window, &windowWidth, &windowHeight);
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
    std::cout << "  W/S         - Raise/lower camera angle" << std::endl;
    std::cout << "  ESC         - Exit" << std::endl;
    std::cout << std::endl;

    // Render loop
    while (!glfwWindowShouldClose(window)) {
        handleInput();
        renderScene();
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    glfwDestroyWindow(window);
    glfwTerminate();

    std::cout << "Application closed." << std::endl;

    return 0;
}