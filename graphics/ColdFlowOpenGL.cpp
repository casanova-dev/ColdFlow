#include <iostream>
#include <cmath>
#include <GL/glut.h>

// ===================================================================
// ColdFlow - OpenGL Warehouse 3D Visualization (CGL CO2)
// ===================================================================
//
// REQUIREMENT: Computer Graphics CO2 - Geometric Transformations
// "Solve real time problems using geometric transformations."
//
// This module implements real-time geometric transformations:
// - TRANSLATION: Move warehouse using W/S/A/D keys (Samyak Bhalerao)
// - SCALING: Zoom warehouse using +/- keys (Sarthak Korde)
// - ROTATION: Rotate warehouse using Q/E keys (Zaki Haque)
// - INTEGRATION: Zone selection & testing (Samarth Tayde)
//
// ===================================================================

// Global variables for window and display
int windowWidth = 1400;
int windowHeight = 900;
int selectedZone = 0;  // 0 = warehouse, 1-6 = individual zones

// ===================================================================
// SAMYAK - CO2 TRANSLATION
// ===================================================================
// Purpose: Move the complete warehouse in real time.
// Real-world problem: Navigate and position warehouse in viewport.
// Implementation: glTranslatef(translateX, translateY, 0) moves entire scene.
// ===================================================================

float translateX = 0.0f;  // Horizontal translation (left/right)
float translateY = 0.0f;  // Vertical translation (up/down)

// ===================================================================
// SARTHAK - CO2 SCALING
// ===================================================================
// Purpose: Zoom warehouse uniformly while preserving proportions.
// Real-world problem: Inspect warehouse detail or get overview.
// Implementation: glScalef(scaleValue, scaleValue, scaleValue) zooms uniformly.
// Minimum scale: 0.4 prevents warehouse from disappearing.
// ===================================================================

float scaleValue = 1.0f;     // Uniform scaling factor
const float MIN_SCALE = 0.4f; // Minimum zoom limit
const float MAX_SCALE = 3.0f; // Maximum zoom limit

// ===================================================================
// ZAKI - CO2 ROTATION
// ===================================================================
// Purpose: Rotate warehouse around Z-axis for viewing from different angles.
// Real-world problem: Inspect warehouse 360° without moving camera.
// Implementation: glRotatef(rotationAngle, 0, 0, 1) rotates around Z.
// Matrix isolation using glPushMatrix()/glPopMatrix() protects text/UI.
// ===================================================================

float rotationAngle = 0.0f;  // Rotation angle in degrees around Z-axis

// Temperature data for visual feedback
struct ZoneStatus {
    float x, y, z;            // Position
    float width, height, depth; // Dimensions
    float temperature;         // Current temperature
    float targetTemperature;   // Target temperature
    float r, g, b;            // Color (RGB)
    const char* name;         // Zone name
};

// Six warehouse zones
ZoneStatus zones[6] = {
    {-6, 1, 0, 3.0f, 4.0f, 3.0f, -18.5f, -20.0f, 0.0f, 0.3f, 1.0f, "FREEZER (-20C)"},
    {0, 1, 0, 3.0f, 4.0f, 3.0f, 4.2f, 4.0f, 0.0f, 0.8f, 0.2f, "CHILLER (4C)"},
    {6, 1, 0, 3.0f, 4.0f, 3.0f, 24.8f, 25.0f, 1.0f, 0.5f, 0.0f, "AMBIENT (25C)"},
    {-6, 1, -4, 3.0f, 4.0f, 3.0f, -19.0f, -20.0f, 0.1f, 0.2f, 0.9f, "DEEP FREEZE"},
    {0, 1, -4, 3.0f, 4.0f, 3.0f, 3.8f, 4.0f, 0.1f, 0.9f, 0.1f, "COLD STORAGE"},
    {6, 1, -4, 3.0f, 4.0f, 3.0f, 25.2f, 25.0f, 1.0f, 0.6f, 0.1f, "DRY STORAGE"}
};

// ===================================================================
// GEOMETRY DRAWING FUNCTIONS
// ===================================================================

// Draw a cube/box (for storage zones)
void drawCube(float x, float y, float z,
              float width, float height, float depth,
              float r, float g, float b) {

    glPushMatrix();
    glTranslatef(x, y, z);
    glColor3f(r, g, b);

    // Front face
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
void drawContainer(float x, float y, float z,
                   float width, float height, float depth,
                   float r, float g, float b, float intensity) {

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

// Draw warehouse floor
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

// Draw warehouse walls
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

// Draw shelving racks
void drawShelf(float x, float y, float z,
               float width, float depth, float height) {

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

// Draw temperature indicator
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

    // Draw indicator pole
    glBegin(GL_LINES);
    glVertex3f(x, y, z);
    glVertex3f(x, y + 1.0f, z);
    glEnd();

    // Draw indicator bulb
    glBegin(GL_TRIANGLES);
    glVertex3f(x, y - 0.2f, z);
    glVertex3f(x - 0.15f, y, z);
    glVertex3f(x + 0.15f, y, z);
    glEnd();
}

// ===================================================================
// TEXT RENDERING (NOT AFFECTED BY TRANSFORMATIONS)
// ===================================================================

void drawText(float x, float y, const char* text) {
    glRasterPos2f(x, y);
    while (*text) {
        glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, *text++);
    }
}

// ===================================================================
// WAREHOUSE RENDERING WITH ZONE SELECTION
// ===================================================================

void renderWarehouse() {
    // Draw warehouse floor and walls
    drawFloor(12.0f);
    drawWalls(12.0f);

    // SAMARTH - CO2 INTEGRATION: Draw all six zones
    for (int i = 0; i < 6; i++) {
        float localScale = 1.0f;
        
        // SAMARTH - CO2 INTEGRATION: Zone selection emphasis
        // If this zone is selected, apply local scaling for visual emphasis
        if (selectedZone == i + 1) {
            localScale = 1.2f;  // Emphasize selected zone
        }

        glPushMatrix();
        glTranslatef(zones[i].x, zones[i].y, zones[i].z);
        glScalef(localScale, localScale, localScale);

        // Draw zone cube
        drawCube(0, 0, 0, zones[i].width, zones[i].height, zones[i].depth,
                 zones[i].r, zones[i].g, zones[i].b);

        // Draw shelves
        drawShelf(-1.5f, -1, -1.5f, zones[i].width, zones[i].depth, zones[i].height);

        // Draw containers
        drawContainer(-0.5f, 0.5f, -0.5f,
                      1.5f, 1.5f, 1.0f,
                      zones[i].r, zones[i].g, zones[i].b, 0.9f);

        drawContainer(0.5f, 0.5f, -0.5f,
                      1.5f, 1.5f, 1.0f,
                      zones[i].r, zones[i].g, zones[i].b, 0.85f);

        // Draw temperature indicator
        drawTemperatureIndicator(0, 2.5f, 0, zones[i].temperature, zones[i].targetTemperature);

        glPopMatrix();
    }
}

// ===================================================================
// DISPLAY CALLBACK
// ===================================================================

void display() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();

    // Camera position (3D perspective view)
    gluLookAt(0.0f, 8.0f, 15.0f,   // Camera position
              0.0f, 2.0f, 0.0f,    // Look at
              0.0f, 1.0f, 0.0f);   // Up vector

    // ===================================================================
    // SAMYAK - CO2 TRANSLATION
    // ===================================================================
    // Apply translation transformation first
    // glTranslatef(translateX, translateY, 0) moves entire warehouse
    // translateX and translateY modified by W/S/A/D keys in keyboard()
    // ===================================================================
    glTranslatef(translateX, translateY, 0.0f);

    // ===================================================================
    // ZAKI - CO2 ROTATION
    // ===================================================================
    // Apply rotation transformation (around Z-axis)
    // glRotatef(rotationAngle, 0, 0, 1) rotates warehouse 360°
    // Q key: decrease rotationAngle (counter-clockwise)
    // E key: increase rotationAngle (clockwise)
    // Rotation preserves warehouse proportions and zone relationships
    // ===================================================================
    glRotatef(rotationAngle, 0.0f, 0.0f, 1.0f);

    // ===================================================================
    // SARTHAK - CO2 SCALING
    // ===================================================================
    // Apply scaling transformation (uniform on all axes)
    // glScalef(scaleValue, scaleValue, scaleValue) zooms warehouse uniformly
    // +/= keys: zoom in (scaleValue increases, capped at MAX_SCALE)
    // - key: zoom out (scaleValue decreases, limited to MIN_SCALE)
    // Scaling maintains zone proportions and warehouse structure integrity
    // ===================================================================
    glScalef(scaleValue, scaleValue, scaleValue);

    // Render the warehouse with all six zones
    renderWarehouse();

    // ===================================================================
    // ZAKI - CO2 ROTATION: TEXT ISOLATION USING MATRIX STACK
    // ===================================================================
    // Push current matrix state before drawing text/UI
    // This prevents transformations from affecting on-screen labels
    // ===================================================================
    glPushMatrix();

    // Reset transformations for text (undo translation, rotation, scaling)
    glLoadIdentity();
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    glOrtho(0, windowWidth, windowHeight, 0, -1, 1);
    glMatrixMode(GL_MODELVIEW);

    // ===================================================================
    // SAMARTH - CO2 INTEGRATION: DISPLAY TRANSFORMATION STATE
    // ===================================================================
    // Show current transformation values for real-time feedback
    // Assists in viva demonstration of working transformations
    // ===================================================================

    glColor3f(1.0f, 1.0f, 1.0f);
    
    char infoText[256];
    sprintf(infoText, "COLDFLOW CGL CO2 - GEOMETRIC TRANSFORMATIONS");
    drawText(10, 20, infoText);

    sprintf(infoText, "Translation: X=%.2f, Y=%.2f (W/A/S/D)", translateX, translateY);
    drawText(10, 50, infoText);

    sprintf(infoText, "Rotation: Z-axis=%.1f degrees (Q/E)", rotationAngle);
    drawText(10, 80, infoText);

    sprintf(infoText, "Scaling: %.2fx (±=zoom, -=out)", scaleValue);
    drawText(10, 110, infoText);

    sprintf(infoText, "Selected Zone: %d (1-6=select, R=reset)", selectedZone);
    drawText(10, 140, infoText);

    // Instructions
    glColor3f(0.8f, 0.8f, 0.8f);
    sprintf(infoText, "W/A/S/D: Translate | Q/E: Rotate | +/-: Scale | 1-6: Select Zone | R: Reset | ESC: Exit");
    drawText(10, windowHeight - 30, infoText);

    // Restore projection matrix
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
    glPopMatrix();

    glutSwapBuffers();
}

// ===================================================================
// KEYBOARD HANDLER
// ===================================================================

void keyboard(unsigned char key, int x, int y) {
    // ===================================================================
    // SAMYAK - CO2 TRANSLATION
    // ===================================================================
    // Handle W/S/A/D for horizontal and vertical translation
    // W: Move up (positive Y)
    // S: Move down (negative Y)
    // A: Move left (negative X)
    // D: Move right (positive X)
    // ===================================================================

    if (key == 'w' || key == 'W') {
        translateY += 0.5f;  // Move warehouse up
    }
    if (key == 's' || key == 'S') {
        translateY -= 0.5f;  // Move warehouse down
    }
    if (key == 'a' || key == 'A') {
        translateX -= 0.5f;  // Move warehouse left
    }
    if (key == 'd' || key == 'D') {
        translateX += 0.5f;  // Move warehouse right
    }

    // ===================================================================
    // SARTHAK - CO2 SCALING
    // ===================================================================
    // Handle +/- for zoom in/out
    // +/=: Zoom in (increase scaleValue up to MAX_SCALE)
    // -: Zoom out (decrease scaleValue down to MIN_SCALE)
    // ===================================================================

    if (key == '+' || key == '=') {
        scaleValue += 0.1f;
        if (scaleValue > MAX_SCALE) scaleValue = MAX_SCALE;  // Clamp to max
    }
    if (key == '-' || key == '_') {
        scaleValue -= 0.1f;
        if (scaleValue < MIN_SCALE) scaleValue = MIN_SCALE;  // Clamp to min
    }

    // ===================================================================
    // ZAKI - CO2 ROTATION
    // ===================================================================
    // Handle Q/E for clockwise/anticlockwise rotation
    // Q: Rotate anticlockwise (decrease rotationAngle)
    // E: Rotate clockwise (increase rotationAngle)
    // ===================================================================

    if (key == 'q' || key == 'Q') {
        rotationAngle -= 5.0f;  // Anticlockwise rotation
        if (rotationAngle < 0) rotationAngle += 360.0f;  // Wrap around
    }
    if (key == 'e' || key == 'E') {
        rotationAngle += 5.0f;  // Clockwise rotation
        if (rotationAngle >= 360.0f) rotationAngle -= 360.0f;  // Wrap around
    }

    // ===================================================================
    // SAMARTH - CO2 INTEGRATION
    // ===================================================================
    // Handle zone selection (1-6)
    // Handle reset (R) - reset all transformations and selected zone
    // Handle exit (ESC)
    // ===================================================================

    if (key >= '1' && key <= '6') {
        selectedZone = key - '0';  // Select zone 1-6
    }

    if (key == '0') {
        selectedZone = 0;  // Deselect zone
    }

    // SAMARTH - CO2 INTEGRATION: Reset transformation
    if (key == 'r' || key == 'R') {
        translateX = 0.0f;
        translateY = 0.0f;
        rotationAngle = 0.0f;
        scaleValue = 1.0f;
        selectedZone = 0;  // Also reset zone selection
    }

    // Exit
    if (key == 27) {  // ESC key
        exit(0);
    }

    glutPostRedisplay();
}

// ===================================================================
// RESHAPE CALLBACK
// ===================================================================

void reshape(int w, int h) {
    windowWidth = w;
    windowHeight = h;

    glViewport(0, 0, w, h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(45.0, (float)w / (float)h, 0.1f, 100.0f);
    glMatrixMode(GL_MODELVIEW);
}

// ===================================================================
// INITIALIZATION
// ===================================================================

void initOpenGL() {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glEnable(GL_COLOR_MATERIAL);
    glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

    glClearColor(0.1f, 0.1f, 0.15f, 1.0f);  // Dark blue background

    glMatrixMode(GL_PROJECTION);
    gluPerspective(45.0, (float)windowWidth / (float)windowHeight, 0.1f, 100.0f);
    glMatrixMode(GL_MODELVIEW);

    // Set up lighting
    GLfloat light_position[] = {5.0, 8.0, 5.0, 0.0};
    GLfloat light_ambient[] = {0.2, 0.2, 0.2, 1.0};
    GLfloat light_diffuse[] = {1.0, 1.0, 1.0, 1.0};

    glLight(GL_LIGHT0, GL_POSITION, light_position);
    glLight(GL_LIGHT0, GL_AMBIENT, light_ambient);
    glLight(GL_LIGHT0, GL_DIFFUSE, light_diffuse);
}

// ===================================================================
// MAIN
// ===================================================================

int main(int argc, char** argv) {
    std::cout << "========================================" << std::endl;
    std::cout << "ColdFlow OpenGL Warehouse - CGL CO2" << std::endl;
    std::cout << "Geometric Transformations" << std::endl;
    std::cout << "========================================" << std::endl;
    std::cout << std::endl;

    std::cout << "TRANSFORMATION CONTROLS:" << std::endl;
    std::cout << "  W/A/S/D   - Translate warehouse (up/down/left/right)" << std::endl;
    std::cout << "  Q/E       - Rotate warehouse (Z-axis)" << std::endl;
    std::cout << "  +/- / -   - Scale warehouse (zoom in/out)" << std::endl;
    std::cout << "  1-6       - Select warehouse zone" << std::endl;
    std::cout << "  0         - Deselect zone" << std::endl;
    std::cout << "  R         - Reset all transformations" << std::endl;
    std::cout << "  ESC       - Exit" << std::endl;
    std::cout << std::endl;

    std::cout << "STUDENT ASSIGNMENTS:" << std::endl;
    std::cout << "  Samyak Bhalerao  - TRANSLATION (W/A/S/D)" << std::endl;
    std::cout << "  Sarthak Korde    - SCALING (+/- zoom)" << std::endl;
    std::cout << "  Zaki Haque       - ROTATION (Q/E, matrix stack)" << std::endl;
    std::cout << "  Samarth Tayde    - INTEGRATION & TESTING" << std::endl;
    std::cout << std::endl;

    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(windowWidth, windowHeight);
    glutCreateWindow("ColdFlow - CGL CO2 Geometric Transformations");

    initOpenGL();

    glutDisplayFunc(display);
    glutReshapeFunc(reshape);
    glutKeyboardFunc(keyboard);
    glutIdleFunc(glutPostRedisplay);

    glutMainLoop();

    return 0;
}
