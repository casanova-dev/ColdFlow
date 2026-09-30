; ===================================================================
; ColdFlow - x86-64 Assembly Temperature Control Module
; ===================================================================
;
; REQUIREMENT: COA CO1 - 80386 Architecture and Instruction Set
; Converted to 64-bit x86-64 register model and System V ABI usage.
;
; This module demonstrates:
; - x86-64 register usage (RAX, RBX, RCX, RDX, RSI, RDI, R8)
; - Arithmetic operations (MOV, SUB, CMP)
; - Conditional branching (JG, JL, JE, JMP)
; - Bit manipulation (AND, OR)
; - Control flow and status flag usage
;
; SCENARIO: Process temperature sensor data and determine cooling status
; INPUT: Target temperature (RDI), Current temperature (RSI)
; OUTPUT: Temperature error (RAX), Cooling decision (RDX), Trend (R8)
; ===================================================================

; ===================================================================
; SECTION: x86-64 REGISTER MAPPING
; ===================================================================
;
; RDI - First argument: Target Temperature (in tenths of degrees Celsius)
; RSI - Second argument: Current Temperature (in tenths of degrees Celsius)
; RAX - Accumulator: Temperature Error (result)
; RBX - Base Register: Preserved for local use
; RCX - Counter / temporary value
; RDX - Data Register: Cooling Control Status
; R8  - Trend indicator (1=rising, -1=falling, 0=stable)
; RSP - Stack Pointer: Stack management
; RBP - Base Pointer: Stack frame
;
; Flags Used:
;   - Zero Flag (ZF): Set when result is zero
;   - Sign Flag (SF): Set when result is negative
;   - Carry Flag (CF): Set on arithmetic overflow
;
; ===================================================================

section .data
    ; Temperature threshold constants (in tenths of degrees)
    COOLING_THRESHOLD   equ 20    ; 2.0°C - turn compressor ON
    IDLE_THRESHOLD     equ 5     ; 0.5°C - idle mode
    OFF_THRESHOLD      equ 0     ; 0.0°C - turn OFF

    ; Cooling control codes
    COOLING_ON         equ 1     ; Compressor ON
    COOLING_IDLE       equ 0     ; Idle mode
    COOLING_OFF        equ 0xFFFF ; Compressor OFF

    ; Status display strings (for debugging/logging)
    msg_target         db "Target Temperature: ", 0
    msg_current        db "Current Temperature: ", 0
    msg_error          db "Temperature Error: ", 0
    msg_cooling_on     db "COOLING: ON", 0
    msg_cooling_idle   db "COOLING: IDLE", 0
    msg_cooling_off    db "COOLING: OFF", 0
    msg_temp_rising    db "Trend: RISING", 0
    msg_temp_falling   db "Trend: FALLING", 0
    msg_temp_stable    db "Trend: STABLE", 0

section .text
    global coldflow_temp_control

; ===================================================================
; FUNCTION: coldflow_temp_control
; ===================================================================
; ENTRY PARAMETERS (x86-64 System V ABI):
;   RDI = Target Temperature (in 0.1°C units, signed)
;   RSI = Current Temperature (in 0.1°C units, signed)
;
; EXIT REGISTERS:
;   RAX = Temperature Error (Current - Target)
;   RDX = Cooling Status (1=ON, 0=IDLE, 0xFFFF=OFF)
;   R8  = Trend indicator (1=rising, -1=falling, 0=stable)
;
; INSTRUCTION SEQUENCE:
;   1. Load parameters into registers
;   2. Calculate error: error = current - target
;   3. Determine trend: check sign of error
;   4. Apply hysteresis logic
;   5. Set cooling status in RDX
;   6. Return
; ===================================================================

coldflow_temp_control:
    ; ===============================================================
    ; PROLOG: Save registers for compatibility
    ; ===============================================================
    push rbp
    mov rbp, rsp
    push rbx
    push r12
    push r13

    ; ===============================================================
    ; STEP 1: LOAD PARAMETERS
    ; ===============================================================
    ; Parameters passed via registers:
    ; RDI = Target Temperature
    ; RSI = Current Temperature
    ;
    ; Save them into preserved registers for later use.
    mov r12, rdi                ; R12 = Target Temperature
    mov r13, rsi                ; R13 = Current Temperature

    ; ===============================================================
    ; STEP 2: CALCULATE TEMPERATURE ERROR
    ; ===============================================================
    ; ERROR = CURRENT - TARGET
    ; Formula: RAX = R13 - R12 (current - target)
    ;
    ; Instructions:
    ;   MOV RAX, R13       ; Load current into RAX
    ;   SUB RAX, R12       ; Subtract target from current
    ;                      ; (Sets flags based on result)

    mov rax, r13                ; RAX = Current Temperature
    sub rax, r12                ; RAX = Current - Target (CALCULATE ERROR)
    mov rcx, rax                ; RCX = signed error value

    ; After SUB instruction:
    ; - Zero Flag (ZF) set if RAX == 0 (at target)
    ; - Sign Flag (SF) set if RAX < 0 (too cold)
    ; - RAX contains signed error value

    ; ===============================================================
    ; STEP 3: DETERMINE TEMPERATURE TREND
    ; ===============================================================
    ; Analyze the sign of error to determine trend.
    ; Positive error (current > target) = too warm = trend rising
    ; Negative error (current < target) = too cold = trend falling
    ;
    ; Instructions:
    ;   CMP RCX, 0         ; Compare error with zero
    ;   JG label_rising    ; Jump if Greater (SF=0, ZF=0)
    ;   JL label_falling   ; Jump if Less (signed)
    ;   JE label_stable    ; Jump if Equal (ZF=1)

    xor r8, r8                  ; R8 = 0 (will hold trend indicator)

    cmp rcx, 0                  ; Compare error with zero
                                ; Sets flags: ZF, SF, CF

    jg  .trend_rising           ; If error > 0: too warm, trend RISING
    jl  .trend_falling          ; If error < 0: too cold, trend FALLING
    je  .trend_stable           ; If error == 0: stable

    ; ===============================================================
    ; TREND_FALLING: Current temperature below target
    ; ===============================================================
    .trend_falling:
        mov r8, -1              ; R8 = -1 (falling trend)
        jmp .check_cooling

    ; ===============================================================
    ; TREND_RISING: Current temperature above target
    ; ===============================================================
    .trend_rising:
        mov r8, 1               ; R8 = 1 (rising trend)
        jmp .check_cooling

    ; ===============================================================
    ; TREND_STABLE: Current equals target
    ; ===============================================================
    .trend_stable:
        mov r8, 0               ; R8 = 0 (stable)
        ; Fall through to cooling check

    ; ===============================================================
    ; STEP 4: HYSTERESIS CONTROL - DETERMINE COOLING STATUS
    ; ===============================================================
    ; Apply deadband/hysteresis logic to prevent chattering.
    ;
    ; Logic:
    ;   IF error > COOLING_THRESHOLD (20 = 2.0°C)
    ;       THEN cooling = ON
    ;   ELSE IF error <= IDLE_THRESHOLD (5 = 0.5°C)
    ;       THEN cooling = OFF
    ;   ELSE (5 < error <= 20)
    ;       THEN cooling = IDLE
    ;
    ; Register usage for comparisons:
    ;   RCX = Temperature Error (already calculated)
    ;   RDX = Will hold cooling status
    ;   RAX = current threshold value

    .check_cooling:
        ; Default: set cooling to OFF (0xFFFF)
        mov rdx, 0xFFFF         ; RDX = COOLING_OFF

        ; Compare error with COOLING_THRESHOLD
        mov rax, 20             ; RAX = COOLING_THRESHOLD (2.0°C)
        cmp rcx, rax            ; Compare error with threshold
                                ; Flags set based on (RCX - RAX)

        jg  .cooling_on         ; If error > 20: turn COOLING ON

        ; Now check against IDLE_THRESHOLD
        mov rax, 5              ; RAX = IDLE_THRESHOLD (0.5°C)
        cmp rcx, rax            ; Compare error with idle threshold

        jle .cooling_off        ; If error <= 5: turn COOLING OFF

        ; ===============================================================
        ; COOLING_IDLE: Error is between thresholds
        ; ===============================================================
        ; 5 < error <= 20: maintain current state (IDLE)
        mov rdx, 0              ; RDX = COOLING_IDLE
        jmp .apply_trend_logic

        ; ===============================================================
        ; COOLING_ON: Error exceeds threshold
        ; ===============================================================
        .cooling_on:
            ; Check if trend is falling (temperature decreasing)
            ; If falling, can reduce cooling intensity
            cmp r8, -1           ; Check if trend == -1 (falling)
            je  .cooling_idle_on_falling ; If falling, go IDLE instead

            mov rdx, 1           ; RDX = COOLING_ON
            jmp .apply_trend_logic

            .cooling_idle_on_falling:
                mov rdx, 0       ; RDX = COOLING_IDLE (reduce intensity)
                jmp .apply_trend_logic

        ; ===============================================================
        ; COOLING_OFF: Error below threshold
        ; ===============================================================
        .cooling_off:
            mov rdx, 0xFFFF     ; RDX = COOLING_OFF
            ; Fall through to apply_trend_logic

    ; ===============================================================
    ; STEP 5: APPLY TREND LOGIC (Optional refinement)
    ; ===============================================================
    ; If temperature is rising too fast, force cooling ON.

    .apply_trend_logic:
        ; Check if error is positive AND trend is rising
        cmp rcx, 10             ; If error > 1.0°C
        jle .finalize_output

        cmp r8, 1               ; AND trend is rising
        jne .finalize_output

        ; Force cooling ON for rising temperatures with large error
        mov rdx, 1              ; RDX = COOLING_ON

    ; ===============================================================
    ; STEP 6: FINALIZE OUTPUT AND RETURN
    ; ===============================================================

    .finalize_output:
        ; Ensure RAX contains the temperature error.
        mov rax, rcx

        ; ===============================================================
        ; EPILOG: Restore registers and return
        ; ===============================================================
        pop r13
        pop r12
        pop rbx
        pop rbp

        ret                     ; Return to caller
                                ; RAX = Temperature Error
                                ; RDX = Cooling Status
                                ; R8  = Trend Indicator

; ===================================================================
; INSTRUCTION SET SUMMARY
; ===================================================================
;
; MOV dest, src     - Move data: MOV RAX, RBX (copy RBX to RAX)
; SUB dest, src     - Subtract: SUB RAX, R12 (RAX = RAX - R12)
; CMP dest, src     - Compare: CMP RCX, 0 (set flags, no result stored)
; JG label          - Jump if Greater (signed): SF=0, ZF=0
; JL label          - Jump if Less (signed): SF!=ZF
; JE label          - Jump if Equal: ZF=1
; JLE label         - Jump if Less or Equal: (SF!=ZF) OR ZF=1
; JMP label         - Unconditional jump
; PUSH reg          - Push register onto stack
; POP reg           - Pop register from stack
; XOR reg, reg      - Bitwise XOR (clear register: XOR RAX, RAX)
; RET               - Return from subroutine
;
; FLAGS (64-bit RFLAGS register):
;   ZF (bit 6) - Zero Flag: Set if result is zero
;   SF (bit 7) - Sign Flag: Set if result is negative
;   CF (bit 0) - Carry Flag: Set on overflow/underflow
;
; ===================================================================

; ===================================================================
; END OF x86-64 ASSEMBLY MODULE
; ===================================================================
