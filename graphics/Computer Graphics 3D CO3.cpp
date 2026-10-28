#include <GL/freeglut.h>
#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <algorithm>
#include <string>

// ColdFlow 3D: CO1 scene, CO2 transformations, CO3 arithmetic panel.
// Fixed-function OpenGL / FreeGLUT educational visualization.

float moveX = 0.0f, moveZ = 0.0f, sceneScale = 1.0f, sceneAngle = 0.0f;
float cameraAngle = 0.0f;
int selectedZone = 1;
int operandA = 13, operandB = 5, arithmeticOp = 0;
float temperature[6] = {-18.0f, -16.5f, 3.5f, 4.0f, 22.0f, 21.0f};
float targetTemp[6] = {-18.0f, -18.0f, 4.0f, 4.0f, 22.0f, 22.0f};

void drawText(float x, float y, const char* s) {
    glRasterPos2f(x, y);
    for (int i = 0; s[i] != '\0'; ++i) glutBitmapCharacter(GLUT_BITMAP_8_BY_13, s[i]);
}

void statusColor(int z) {
    float diff = std::fabs(temperature[z - 1] - targetTemp[z - 1]);
    if (diff <= 1.5f) glColor3f(0.18f, 0.78f, 0.36f);
    else if (diff <= 3.0f) glColor3f(1.0f, 0.66f, 0.12f);
    else glColor3f(0.92f, 0.20f, 0.18f);
}

void solidBox(float x, float y, float z, float sx, float sy, float sz) {
    glPushMatrix();
    glTranslatef(x, y, z);
    glScalef(sx, sy, sz);
    glutSolidCube(1.0);
    glPopMatrix();
}

void wireBox(float x, float y, float z, float sx, float sy, float sz) {
    glPushMatrix();
    glTranslatef(x, y, z);
    glScalef(sx, sy, sz);
    glutWireCube(1.0);
    glPopMatrix();
}
// Samyak 
void drawRack(float x, float z) {
    // Steel uprights and shelves, all in 3D.
    glColor3f(0.58f, 0.68f, 0.78f);
    solidBox(x - 0.42f, 0.75f, z, 0.07f, 1.5f, 0.07f);
    solidBox(x + 0.42f, 0.75f, z, 0.07f, 1.5f, 0.07f);
    for (int level = 0; level < 3; ++level) {
        float y = 0.25f + level * 0.48f;
        solidBox(x, y, z, 0.9f, 0.06f, 0.52f);
    }
    // Cartons on the rack shelves.
    for (int level = 0; level < 3; ++level) {
        float y = 0.39f + level * 0.48f;
        for (int c = 0; c < 2; ++c) {
            glColor3f(0.20f + 0.08f * c, 0.48f + 0.08f * level, 0.82f);
            solidBox(x - 0.20f + c * 0.40f, y, z, 0.30f, 0.22f, 0.30f);
        }
    }
}

void drawZone(float x, float z, int zoneId) {
    glPushMatrix();
    glTranslatef(x, 0.0f, z);

    // Selected zone is enlarged and outlined.
    if (zoneId == selectedZone) glScalef(1.08f, 1.0f, 1.08f);

    // Floor platform.
    statusColor(zoneId);
    solidBox(0.0f, -0.08f, 0.0f, 2.35f, 0.16f, 2.05f);

    // Perimeter frame.
    glColor3f(zoneId == selectedZone ? 1.0f : 0.72f,
              zoneId == selectedZone ? 0.92f : 0.80f,
              zoneId == selectedZone ? 0.25f : 0.90f);
    wireBox(0.0f, 0.85f, 0.0f, 2.35f, 1.75f, 2.05f);

    drawRack(-0.55f, -0.30f);
    drawRack(0.55f, -0.30f);

    // A colored temperature indicator at the front of each zone.
    statusColor(zoneId);
    solidBox(0.0f, 1.80f, 0.90f, 0.72f, 0.18f, 0.12f);
    glPopMatrix();
}

void drawWarehouse3D() {
    glPushMatrix();
    glTranslatef(moveX, 0.0f, moveZ);
    glRotatef(sceneAngle, 0.0f, 1.0f, 0.0f);
    glScalef(sceneScale, sceneScale, sceneScale);

    // Six zones arranged as two rows of three.
    drawZone(-2.6f, -2.2f, 1);
    drawZone( 0.0f, -2.2f, 2);
    drawZone( 2.6f, -2.2f, 3);
    drawZone(-2.6f,  0.2f, 4);
    drawZone( 0.0f,  0.2f, 5);
    drawZone( 2.6f,  0.2f, 6);

    // Warehouse roof beams and support columns.
    glColor3f(0.40f, 0.50f, 0.62f);
    solidBox(0.0f, 2.05f, -1.0f, 8.1f, 0.12f, 5.2f);
    glPopMatrix();
}

std::string toBinary(unsigned int v) {
    std::string s;
    for (int i = 7; i >= 0; --i) s += ((v >> i) & 1U) ? '1' : '0';
    return s;
}

void drawOverlay() {
    int w = glutGet(GLUT_WINDOW_WIDTH), h = glutGet(GLUT_WINDOW_HEIGHT);
    glMatrixMode(GL_PROJECTION);
    glPushMatrix(); glLoadIdentity();
    gluOrtho2D(0, w, 0, h);
    glMatrixMode(GL_MODELVIEW);
    glPushMatrix(); glLoadIdentity();
    glDisable(GL_DEPTH_TEST);
    glColor3f(0.92f, 0.96f, 1.0f);

    drawText(18, h - 25, "COLDFLOW 3D - SMART COLD-CHAIN WAREHOUSE");
    drawText(18, h - 45, "CO1: 3D scene | CO2: transformations | CO3: computer arithmetic");

    char s[160];
    std::snprintf(s, sizeof(s), "Selected Zone: %d | Temperature: %.1f C | Target: %.1f C",
                  selectedZone, temperature[selectedZone - 1], targetTemp[selectedZone - 1]);
    drawText(18, h - 65, s);

    long long result = 0;
    const char* opName = "ADDITION";
    if (arithmeticOp == 0) result = operandA + operandB;
    if (arithmeticOp == 1) { result = operandA - operandB; opName = "SUBTRACTION"; }
    if (arithmeticOp == 2) { result = (long long)operandA * operandB; opName = "MULTIPLICATION"; }
    if (arithmeticOp == 3) { opName = "DIVISION"; if (operandB != 0) result = operandA / operandB; }

    int right = std::max(18, w - 300);
    std::snprintf(s, sizeof(s), "CO3: %s", opName); drawText(right, h - 25, s);
    std::snprintf(s, sizeof(s), "A=%d (%s)", operandA, toBinary((unsigned)operandA).c_str());
    drawText(right, h - 45, s);
    std::snprintf(s, sizeof(s), "B=%d (%s)", operandB, toBinary((unsigned)operandB).c_str());
    drawText(right, h - 65, s);
    if (arithmeticOp == 3 && operandB == 0) drawText(right, h - 85, "Error: divide by zero");
    else {
        std::snprintf(s, sizeof(s), "Result = %lld", result);
        drawText(right, h - 85, s);
    }

    drawText(18, 68, "W/S: move depth | A/D: move sideways | Q/E: rotate | +/-: scale");
    drawText(18, 48, "1-6: select zone | Arrow keys: orbit camera | R: reset | ESC: exit");
    drawText(18, 28, "F1 add | F2 subtract | F3 multiply | F4 divide | [ ] change A | ; ' change B");

    glEnable(GL_DEPTH_TEST);
    glPopMatrix();
    glMatrixMode(GL_PROJECTION); glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
}

void display() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // Orbit camera around the warehouse.
    glTranslatef(0.0f, -0.3f, -15.0f);
    glRotatef(cameraAngle, 0.0f, 1.0f, 0.0f);
    glRotatef(18.0f, 1.0f, 0.0f, 0.0f);

    // Floor grid.
    glColor3f(0.18f, 0.23f, 0.29f);
    glBegin(GL_LINES);
    for (int i = -10; i <= 10; ++i) {
        glVertex3f((float)i, -0.18f, -8.0f); glVertex3f((float)i, -0.18f, 8.0f);
        glVertex3f(-10.0f, -0.18f, (float)i); glVertex3f(10.0f, -0.18f, (float)i);
    }
    glEnd();

    // Lighting for a clearer 3D appearance.
    glEnable(GL_LIGHTING); glEnable(GL_LIGHT0); glEnable(GL_COLOR_MATERIAL);
    GLfloat lightPos[] = {-5.0f, 8.0f, 6.0f, 1.0f};
    glLightfv(GL_LIGHT0, GL_POSITION, lightPos);
    glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
    drawWarehouse3D();
    glDisable(GL_LIGHTING);

    drawOverlay();
    glutSwapBuffers();
}

void keyboard(unsigned char key, int, int) {
    switch (key) {
        case 'w': case 'W': moveZ -= 0.25f; break;
        case 's': case 'S': moveZ += 0.25f; break;
        case 'a': case 'A': moveX -= 0.25f; break;
        case 'd': case 'D': moveX += 0.25f; break;
        case '+': case '=': sceneScale = std::min(1.5f, sceneScale + 0.05f); break;
        case '-': sceneScale = std::max(0.55f, sceneScale - 0.05f); break;
        case 'q': case 'Q': sceneAngle += 5.0f; break;
        case 'e': case 'E': sceneAngle -= 5.0f; break;
        case '1': selectedZone = 1; break; case '2': selectedZone = 2; break;
        case '3': selectedZone = 3; break; case '4': selectedZone = 4; break;
        case '5': selectedZone = 5; break; case '6': selectedZone = 6; break;
        case '[': operandA = std::max(0, operandA - 1); break;
        case ']': operandA = std::min(99, operandA + 1); break;
        case ';': operandB = std::max(0, operandB - 1); break;
        case '\'': operandB = std::min(99, operandB + 1); break;
        case 'r': case 'R':
            moveX = moveZ = sceneAngle = cameraAngle = 0.0f;
            sceneScale = 1.0f; selectedZone = 1; break;
        case 27: std::exit(0);
    }
    glutPostRedisplay();
}

void specialKeyboard(int key, int, int) {
    if (key == GLUT_KEY_F1) arithmeticOp = 0;
    if (key == GLUT_KEY_F2) arithmeticOp = 1;
    if (key == GLUT_KEY_F3) arithmeticOp = 2;
    if (key == GLUT_KEY_F4) arithmeticOp = 3;
    if (key == GLUT_KEY_LEFT) cameraAngle -= 5.0f;
    if (key == GLUT_KEY_RIGHT) cameraAngle += 5.0f;
    glutPostRedisplay();
}

void reshape(int w, int h) {
    if (h == 0) h = 1;
    glViewport(0, 0, w, h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(55.0, (double)w / (double)h, 0.1, 100.0);
    glMatrixMode(GL_MODELVIEW);
}

int main(int argc, char** argv) {
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(1280, 800);
    glutCreateWindow("ColdFlow 3D - CO1 CO2 CO3");
    glClearColor(0.035f, 0.055f, 0.085f, 1.0f);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_NORMALIZE);
    glutDisplayFunc(display);
    glutKeyboardFunc(keyboard);
    glutSpecialFunc(specialKeyboard);
    glutReshapeFunc(reshape);
    glutMainLoop();
    return 0;
}
