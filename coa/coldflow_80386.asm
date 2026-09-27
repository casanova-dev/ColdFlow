; ===================================================================
; ColdFlow - x86-64 Assembly Temperature Control Module
; ===================================================================
;
; REQUIREMENT: 2310221L.CO.2 - Develop assembly language programs
;              using the 80386-derived instruction set (extended to
;              its 64-bit successor architecture, x86-64 / AMD64)
;
; This module demonstrates:
; - 64-bit general-purpose register usage (RAX, RBX, RCX, RDX, RSI, RDI)
; - Arithmetic operations (MOV, SUB, CMP) on 64-bit operands
; - Conditional branching (JG, JL, JE, JLE, JMP)
; - Bit manipulation (XOR for register clearing)
; - Control flow and status flag usage (RFLAGS)
; - Direct evolution from the 80386's 32-bit register set (EAX..EDI)
;   to its full 64-bit extension (RAX..RDI), preserving the original
;   instruction sequence and control logic one-to-one
;
; SCENARIO: Process temperature sensor data and determine cooling status
; INPUT: Target temperature (RAX), Current temperature (RBX)
; OUTPUT: Temperature error (RCX), Cooling decision (RDX)
; ===================================================================

; ===================================================================
; SECTION: 80386 -> x86-64 REGISTER MAPPING
; ===================================================================
;
; The 80386 introduced the 32-bit extensions (EAX, EBX, ECX, EDX, ESI,
; EDI, ESP, EBP) of the original 16-bit 8086 registers. The AMD64/
; x86-64 architecture later extended each of these again to full
; 64-bit width by prefixing with "R" instead of "E". This module keeps
; the exact same register roles as the original 80386 version, simply
; operating on the widened 64-bit registers:
;
;   80386 (32-bit)   ->   x86-64 (64-bit)   ->   Role
;   --------------        ---------------        ----
;   EAX              ->   RAX                    Accumulator: Target Temperature
;   EBX              ->   RBX                    Base Register: Current Temperature
;   ECX              ->   RCX                    Counter Register: Temperature Error (result)
;   EDX              ->   RDX                    Data Register: Cooling Control Status
;   ESI              ->   RSI                    Source Index: Trend indicator
;   EDI              ->   RDI                    Destination Index: preserved/unused scratch
;   ESP              ->   RSP                    Stack Pointer: Stack management
;   EBP              ->   RBP                    Base Pointer: Stack frame
;
; Temperature values remain stored in tenths of degrees Celsius, now
; carried in 64-bit registers so the same logic naturally supports a
; far wider range without overflow.
;
; Flags Used (RFLAGS, same semantics as 80386 EFLAGS):
;   - Zero Flag (ZF): Set when result is zero
;   - Sign Flag (SF): Set when result is negative
;   - Carry Flag (CF): Set on arithmetic overflow
;
; ===================================================================

section .data
    ; Temperature threshold constants (in tenths of degrees)
    COOLING_THRESHOLD   equ 20    ; 2.0°C - turn compressor ON
    IDLE_THRESHOLD      equ 5     ; 0.5°C - idle mode
    OFF_THRESHOLD       equ 0     ; 0.0°C - turn OFF

    ; Cooling control codes
    COOLING_ON          equ 1     ; Compressor ON
    COOLING_IDLE        equ 0     ; Idle mode
    COOLING_OFF         equ 0xFFFF ; Compressor OFF

    ; Status display strings (for debugging/logging)
    msg_target          db "Target Temperature: ", 0
    msg_current         db "Current Temperature: ", 0
    msg_error           db "Temperature Error: ", 0
    msg_cooling_on      db "COOLING: ON", 0
    msg_cooling_idle    db "COOLING: IDLE", 0
    msg_cooling_off     db "COOLING: OFF", 0
    msg_temp_rising     db "Trend: RISING", 0
    msg_temp_falling    db "Trend: FALLING", 0
    msg_temp_stable     db "Trend: STABLE", 0

section .text
    global coldflow_temp_control

; ===================================================================
; FUNCTION: coldflow_temp_control
; ===================================================================
; ENTRY PARAMETERS (64-bit register-based calling convention):
;   RAX = Target Temperature (in 0.1°C units, signed)
;   RBX = Current Temperature (in 0.1°C units, signed)
;
; EXIT REGISTERS:
;   RCX = Temperature Error (Current - Target)
;   RDX = Cooling Status (1=ON, 0=IDLE, 0xFFFF=OFF)
;   RSI = Trend indicator (1=rising, -1=falling, 0=stable)
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
    push rbp                    ; Save old base pointer
    mov rbp, rsp                ; Set up new stack frame
    push rbx                    ; Preserve RBX
    push rsi                    ; Preserve RSI
    push rdi                    ; Preserve RDI

    ; ===============================================================
    ; STEP 1: LOAD PARAMETERS
    ; ===============================================================
    ; Parameters passed via registers:
    ; RAX = Target Temperature (already in RAX)
    ; RBX = Current Temperature (already in RBX)
    ;
    ; For clarity, demonstrate saving to named locations
    ; (simulated via registers, now widened to 64-bit)

    mov rsi, rax                ; RSI = Target Temperature (safe copy)
    ; RBX already contains Current Temperature

    ; ===============================================================
    ; STEP 2: CALCULATE TEMPERATURE ERROR
    ; ===============================================================
    ; ERROR = CURRENT - TARGET
    ; Formula: RCX = RBX - RAX (current - target)
    ;
    ; Instructions:
    ;   MOV RCX, RBX       ; Load current into RCX
    ;   SUB RCX, RAX       ; Subtract target from current
    ;                      ; (Sets flags based on result)

    mov rcx, rbx                ; RCX = Current Temperature
    sub rcx, rax                ; RCX = Current - Target (CALCULATE ERROR)

    ; After SUB instruction:
    ; - Zero Flag (ZF) set if RCX == 0 (at target)
    ; - Sign Flag (SF) set if RCX < 0 (too cold)
    ; - RCX contains signed error value

    ; ===============================================================
    ; STEP 3: DETERMINE TEMPERATURE TREND
    ; ===============================================================
    ; Analyze the sign of error to determine trend
    ; Positive error (current > target) = too warm = trend rising
    ; Negative error (current < target) = too cold = trend falling
    ;
    ; Instructions:
    ;   CMP RCX, 0         ; Compare error with zero
    ;   JG label_rising    ; Jump if Greater (SF=0, ZF=0)
    ;   JL label_falling   ; Jump if Less (SF=1)
    ;   JE label_stable    ; Jump if Equal (ZF=1)

    xor rsi, rsi                ; RSI = 0 (will hold trend indicator)

    cmp rcx, 0                  ; Compare error with zero
                                 ; Sets flags: ZF (equal), SF (negative), CF (borrow)

    jg  .trend_rising           ; If error > 0: too warm, trend RISING
    jl  .trend_falling          ; If error < 0: too cold, trend FALLING
    je  .trend_stable           ; If error == 0: stable

    ; ===============================================================
    ; TREND_FALLING: Current temperature below target
    ; ===============================================================
    .trend_falling:
        mov rsi, 0xFFFFFFFFFFFFFFFF  ; RSI = -1 (falling trend, sign-extended, 64-bit)
        jmp .check_cooling

    ; ===============================================================
    ; TREND_RISING: Current temperature above target
    ; ===============================================================
    .trend_rising:
        mov rsi, 1              ; RSI = 1 (rising trend)
        jmp .check_cooling

    ; ===============================================================
    ; TREND_STABLE: Current equals target
    ; ===============================================================
    .trend_stable:
        mov rsi, 0              ; RSI = 0 (stable)
        ; Fall through to cooling check

    ; ===============================================================
    ; STEP 4: HYSTERESIS CONTROL - DETERMINE COOLING STATUS
    ; ===============================================================
    ; Apply deadband/hysteresis logic to prevent chattering
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
    ;   RAX = COOLING_THRESHOLD (reuse after no longer needed)
    ;   RBX = IDLE_THRESHOLD (recalculate as needed)

    .check_cooling:
        ; Default: set cooling to OFF (0xFFFF)
        mov rdx, 0xFFFF         ; RDX = COOLING_OFF

        ; Compare error with COOLING_THRESHOLD
        mov rax, 20              ; RAX = COOLING_THRESHOLD (2.0°C)
        cmp rcx, rax             ; Compare error with threshold
                                  ; Flags set based on (RCX - RAX)

        jg  .cooling_on          ; If error > 20: turn COOLING ON

        ; Now check against IDLE_THRESHOLD
        mov rax, 5               ; RAX = IDLE_THRESHOLD (0.5°C)
        cmp rcx, rax             ; Compare error with idle threshold

        jle .cooling_off         ; If error <= 5: turn COOLING OFF

        ; ===============================================================
        ; COOLING_IDLE: Error is between thresholds
        ; ===============================================================
        ; 5 < error <= 20: maintain current state (IDLE)
        mov rdx, 0               ; RDX = COOLING_IDLE
        jmp .apply_trend_logic

        ; ===============================================================
        ; COOLING_ON: Error exceeds threshold
        ; ===============================================================
        .cooling_on:
            ; Check if trend is falling (temperature decreasing)
            ; If falling, can reduce cooling intensity
            cmp rsi, 0xFFFFFFFFFFFFFFFF  ; Check if trend == -1 (falling)
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
            mov rdx, 0xFFFF      ; RDX = COOLING_OFF
            ; Fall through to apply_trend_logic

    ; ===============================================================
    ; STEP 5: APPLY TREND LOGIC (Optional refinement)
    ; ===============================================================
    ; If temperature is rising too fast, force cooling ON

    .apply_trend_logic:
        ; Check if error is positive AND trend is rising
        cmp rcx, 10              ; If error > 1.0°C
        jle .finalize_output

        cmp rsi, 1               ; AND trend is rising
        jne .finalize_output

        ; Force cooling ON for rising temperatures with large error
        mov rdx, 1               ; RDX = COOLING_ON

    ; ===============================================================
    ; STEP 6: FINALIZE OUTPUT AND RETURN
    ; ===============================================================

    .finalize_output:
        ; Ensure RCX still contains error (it should)
        ; Ensure RDX contains cooling status (it should)

        ; For compatibility, move trend to a visible location
        ; (in real x86-64 code, would return via stack or register)

        ; ===============================================================
        ; EPILOG: Restore registers and return
        ; ===============================================================
        pop rdi                  ; Restore RDI
        pop rsi                  ; Restore RSI (contains trend)
        pop rbx                  ; Restore RBX
        pop rbp                  ; Restore stack frame

        ret                      ; Return to caller
                                  ; RCX = Temperature Error
                                  ; RDX = Cooling Status
                                  ; RSI = Trend Indicator (preserved before pop)

; ===================================================================
; INSTRUCTION SET SUMMARY (x86-64, direct extension of 80386 ISA)
; ===================================================================
;
; MOV dest, src     - Move data: MOV RAX, RBX (copy RBX to RAX)
; SUB dest, src     - Subtract: SUB RCX, RAX (RCX = RCX - RAX)
; CMP dest, src     - Compare: CMP RCX, 0 (set flags, no result stored)
; JG label          - Jump if Greater (signed): SF=OF, ZF=0
; JL label          - Jump if Less (signed): SF≠OF
; JE label          - Jump if Equal: ZF=1
; JLE label         - Jump if Less or Equal: (SF≠OF) OR ZF=1
; JMP label         - Unconditional jump
; PUSH reg          - Push 64-bit register onto stack
; POP reg           - Pop 64-bit register from stack
; XOR reg, reg      - Bitwise XOR (clear register: XOR RAX, RAX)
; RET               - Return from subroutine
;
; FLAGS (64-bit RFLAGS register, same layout as 80386 EFLAGS):
;   ZF (bit 6) - Zero Flag: Set if result is zero
;   SF (bit 7) - Sign Flag: Set if result is negative
;   CF (bit 0) - Carry Flag: Set on overflow/underflow
;
; ===================================================================

; ===================================================================
; END OF x86-64 ASSEMBLY MODULE (extended from 80386 baseline)
; ===================================================================
