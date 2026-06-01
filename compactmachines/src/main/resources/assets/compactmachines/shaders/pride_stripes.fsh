#version 330

// pride_stripes — horizontal stripes from a Java-controlled palette.
//
// Each registered MachineFlag compiles its own pipeline variant of this shader
// via shader defines:
//   PALETTE_SIZE         — number of active stripes, 1..10. Float (cast to int).
//   C{i}_R / C{i}_G / C{i}_B — normalised [0,1] colour components for stripe i,
//                              top-to-bottom (i=0 is the top stripe). Slots
//                              i=PALETTE_SIZE..9 are ignored.
//
// When no defines are supplied, the shader falls back to the Baker 6-stripe
// pride flag so it's still functional out-of-the-box for hot-reload testing.

#moj_import <minecraft:globals.glsl>

in vec3 worldPos;   // absolute world coordinates; identical at the seam
                    // between adjacent machines, so the stripes line up
                    // continuously across a vertical wall.
in vec3 faceNormal; // outward normal of the pane.

out vec4 fragColor;

const float OPACITY = 0.5;

// ----- palette defines (Baker defaults) ------------------------------------
#ifndef PALETTE_SIZE
#define PALETTE_SIZE 6.0
#endif

#ifndef C0_R
#define C0_R 0.894
#endif
#ifndef C0_G
#define C0_G 0.012
#endif
#ifndef C0_B
#define C0_B 0.012
#endif

#ifndef C1_R
#define C1_R 1.000
#endif
#ifndef C1_G
#define C1_G 0.549
#endif
#ifndef C1_B
#define C1_B 0.000
#endif

#ifndef C2_R
#define C2_R 1.000
#endif
#ifndef C2_G
#define C2_G 0.929
#endif
#ifndef C2_B
#define C2_B 0.000
#endif

#ifndef C3_R
#define C3_R 0.000
#endif
#ifndef C3_G
#define C3_G 0.502
#endif
#ifndef C3_B
#define C3_B 0.149
#endif

#ifndef C4_R
#define C4_R 0.141
#endif
#ifndef C4_G
#define C4_G 0.251
#endif
#ifndef C4_B
#define C4_B 0.557
#endif

#ifndef C5_R
#define C5_R 0.451
#endif
#ifndef C5_G
#define C5_G 0.161
#endif
#ifndef C5_B
#define C5_B 0.510
#endif

#ifndef C6_R
#define C6_R 0.0
#endif
#ifndef C6_G
#define C6_G 0.0
#endif
#ifndef C6_B
#define C6_B 0.0
#endif

#ifndef C7_R
#define C7_R 0.0
#endif
#ifndef C7_G
#define C7_G 0.0
#endif
#ifndef C7_B
#define C7_B 0.0
#endif

#ifndef C8_R
#define C8_R 0.0
#endif
#ifndef C8_G
#define C8_G 0.0
#endif
#ifndef C8_B
#define C8_B 0.0
#endif

#ifndef C9_R
#define C9_R 0.0
#endif
#ifndef C9_G
#define C9_G 0.0
#endif
#ifndef C9_B
#define C9_B 0.0
#endif

const int MAX_STRIPES = 10;
const vec3 PALETTE[MAX_STRIPES] = vec3[MAX_STRIPES](
    vec3(C0_R, C0_G, C0_B),
    vec3(C1_R, C1_G, C1_B),
    vec3(C2_R, C2_G, C2_B),
    vec3(C3_R, C3_G, C3_B),
    vec3(C4_R, C4_G, C4_B),
    vec3(C5_R, C5_G, C5_B),
    vec3(C6_R, C6_G, C6_B),
    vec3(C7_R, C7_G, C7_B),
    vec3(C8_R, C8_G, C8_B),
    vec3(C9_R, C9_G, C9_B)
);

// ----- main ---------------------------------------------------------------
void main() {
    int N = int(PALETTE_SIZE);

    // Side faces vary along Y naturally — use the fractional part of worldPos.y
    // so stripes repeat per-block (one full flag per machine in the vertical).
    // Cap faces are constant in Y, so use the outward normal to pick the
    // extreme stripe: +Y normal → top stripe (palette[0]); -Y → bottom.
    float y;
    if (faceNormal.y > 0.5) {
        y = 0.999;
    } else if (faceNormal.y < -0.5) {
        y = 0.001;
    } else {
        y = fract(worldPos.y);
    }

    // Invert so palette[0] is at the top of the block (matches how flags
    // are described top-to-bottom).
    int idx = int((1.0 - y) * float(N));
    idx = clamp(idx, 0, N - 1);

    fragColor = vec4(PALETTE[idx], OPACITY);
}
