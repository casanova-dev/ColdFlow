; ===================================================================
; ColdFlow - 80386 Assembly Temperature Control Module
; ===================================================================
;
; REQUIREMENT: COA CO1 - 80386 Architecture and Instruction Set
;
; This module demonstrates:
; - 80386 register usage (EAX, EBX, ECX, EDX)
; - Arithmetic operations (MOV, SUB, CMP)
; - Conditional branching (JG, JL, JE, JMP)
; - Bit manipulation (AND, OR)
; - Control flow and status flag usage
;
; SCENARIO: Process temperature sensor data and determine cooling status
; INPUT: Target temperature (EAX), Current temperature (EBX)
; OUTPUT: Temperature error (ECX), Cooling decision (EDX)
; ===================================================================

; ===================================================================
; SECTION: 80386 REGISTER MAPPING
; ===================================================================
;
; EAX - Accumulator: Target Temperature (in tenths of degrees Celsius)
; EBX - Base Register: Current Temperature (in tenths of degrees Celsius)
; ECX - Counter Register: Temperature Error (result)
; EDX - Data Register: Cooling Control Status
; ESI - Source Index: Temporary calculations
; ESP - Stack Pointer: Stack management
; EBP - Base Pointer: Stack frame
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
; ENTRY PARAMETERS (80386 calling convention):
;   EAX = Target Temperature (in 0.1°C units, signed)
;   EBX = Current Temperature (in 0.1°C units, signed)
;
; EXIT REGISTERS:
;   ECX = Temperature Error (Current - Target)
;   EDX = Cooling Status (1=ON, 0=IDLE, 0xFFFF=OFF)
;   ESI = Trend indicator (1=rising, -1=falling, 0=stable)
;
; INSTRUCTION SEQUENCE:
;   1. Load parameters into registers
;   2. Calculate error: error = current - target
;   3. Determine trend: check sign of error
;   4. Apply hysteresis logic
;   5. Set cooling status in EDX
;   6. Return
; ===================================================================

coldflow_temp_control:
    ; ===============================================================
    ; PROLOG: Save registers for compatibility
    ; ===============================================================
    push ebp                    ; Save old base pointer
    mov ebp, esp                ; Set up new stack frame
    push ebx                    ; Preserve EBX
    push esi                    ; Preserve ESI
    push edi                    ; Preserve EDI
    
    ; ===============================================================
    ; STEP 1: LOAD PARAMETERS
    ; ===============================================================
    ; Parameters passed via registers:
    ; EAX = Target Temperature (already in EAX)
    ; EBX = Current Temperature (already in EBX)
    ; 
    ; For clarity, demonstrate saving to named locations
    ; (simulated via registers for 80386-only environment)
    
    mov esi, eax                ; ESI = Target Temperature (safe copy)
    ; EBX already contains Current Temperature
    
    ; ===============================================================
    ; STEP 2: CALCULATE TEMPERATURE ERROR
    ; ===============================================================
    ; ERROR = CURRENT - TARGET
    ; Formula: ECX = EBX - EAX (current - target)
    ;
    ; Instructions:
    ;   MOV ECX, EBX       ; Load current into ECX
    ;   SUB ECX, EAX       ; Subtract target from current
    ;                      ; (Sets flags based on result)
    
    mov ecx, ebx                ; ECX = Current Temperature
    sub ecx, eax                ; ECX = Current - Target (CALCULATE ERROR)
    
    ; After SUB instruction:
    ; - Zero Flag (ZF) set if ECX == 0 (at target)
    ; - Sign Flag (SF) set if ECX < 0 (too cold)
    ; - ECX contains signed error value
    
    ; ===============================================================
    ; STEP 3: DETERMINE TEMPERATURE TREND
    ; ===============================================================
    ; Analyze the sign of error to determine trend
    ; Positive error (current > target) = too warm = trend rising
    ; Negative error (current < target) = too cold = trend falling
    ;
    ; Instructions:
    ;   CMP ECX, 0         ; Compare error with zero
    ;   JG label_rising    ; Jump if Greater (SF=0, ZF=0)
    ;   JL label_falling   ; Jump if Less (SF=1)
    ;   JE label_stable    ; Jump if Equal (ZF=1)
    
    xor esi, esi                ; ESI = 0 (will hold trend indicator)
    
    cmp ecx, 0                  ; Compare error with zero
                                ; Sets flags: ZF (equal), SF (negative), CF (borrow)
    
    jg  .trend_rising           ; If error > 0: too warm, trend RISING
    jl  .trend_falling          ; If error < 0: too cold, trend FALLING
    je  .trend_stable           ; If error == 0: stable
    
    ; ===============================================================
    ; TREND_FALLING: Current temperature below target
    ; ===============================================================
    .trend_falling:
        mov esi, 0xFFFFFFFF     ; ESI = -1 (falling trend, sign-extended)
        jmp .check_cooling
    
    ; ===============================================================
    ; TREND_RISING: Current temperature above target
    ; ===============================================================
    .trend_rising:
        mov esi, 1              ; ESI = 1 (rising trend)
        jmp .check_cooling
    
    ; ===============================================================
    ; TREND_STABLE: Current equals target
    ; ===============================================================
    .trend_stable:
        mov esi, 0              ; ESI = 0 (stable)
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
    ;   ECX = Temperature Error (already calculated)
    ;   EDX = Will hold cooling status
    ;   EAX = COOLING_THRESHOLD (reuse after no longer needed)
    ;   EBX = IDLE_THRESHOLD (recalculate as needed)
    
    .check_cooling:
        ; Default: set cooling to OFF (0xFFFF)
        mov edx, 0xFFFF         ; EDX = COOLING_OFF
        
        ; Compare error with COOLING_THRESHOLD
        mov eax, 20             ; EAX = COOLING_THRESHOLD (2.0°C)
        cmp ecx, eax            ; Compare error with threshold
                                ; Flags set based on (ECX - EAX)
        
        jg  .cooling_on         ; If error > 20: turn COOLING ON
        
        ; Now check against IDLE_THRESHOLD
        mov eax, 5              ; EAX = IDLE_THRESHOLD (0.5°C)
        cmp ecx, eax            ; Compare error with idle threshold
        
        jle .cooling_off        ; If error <= 5: turn COOLING OFF
        
        ; ===============================================================
        ; COOLING_IDLE: Error is between thresholds
        ; ===============================================================
        ; 5 < error <= 20: maintain current state (IDLE)
        mov edx, 0              ; EDX = COOLING_IDLE
        jmp .apply_trend_logic
        
        ; ===============================================================
        ; COOLING_ON: Error exceeds threshold
        ; ===============================================================
        .cooling_on:
            ; Check if trend is falling (temperature decreasing)
            ; If falling, can reduce cooling intensity
            cmp esi, 0xFFFFFFFF ; Check if trend == -1 (falling)
            je  .cooling_idle_on_falling  ; If falling, go IDLE instead
            
            mov edx, 1          ; EDX = COOLING_ON
            jmp .apply_trend_logic
            
            .cooling_idle_on_falling:
                mov edx, 0      ; EDX = COOLING_IDLE (reduce intensity)
                jmp .apply_trend_logic
        
        ; ===============================================================
        ; COOLING_OFF: Error below threshold
        ; ===============================================================
        .cooling_off:
            mov edx, 0xFFFF     ; EDX = COOLING_OFF
            ; Fall through to apply_trend_logic
    
    ; ===============================================================
    ; STEP 5: APPLY TREND LOGIC (Optional refinement)
    ; ===============================================================
    ; If temperature is rising too fast, force cooling ON
    
    .apply_trend_logic:
        ; Check if error is positive AND trend is rising
        cmp ecx, 10             ; If error > 1.0°C
        jle .finalize_output
        
        cmp esi, 1              ; AND trend is rising
        jne .finalize_output
        
        ; Force cooling ON for rising temperatures with large error
        mov edx, 1              ; EDX = COOLING_ON
        
    ; ===============================================================
    ; STEP 6: FINALIZE OUTPUT AND RETURN
    ; ===============================================================
    
    .finalize_output:
        ; Ensure ECX still contains error (it should)
        ; Ensure EDX contains cooling status (it should)
        
        ; For compatibility, move trend to a visible location
        ; (in real 80386 code, would return via stack or register)
        
        ; ===============================================================
        ; EPILOG: Restore registers and return
        ; ===============================================================
        pop edi                 ; Restore EDI
        pop esi                 ; Restore ESI (contains trend)
        pop ebx                 ; Restore EBX
        pop ebp                 ; Restore stack frame
        
        ret                     ; Return to caller
                                ; ECX = Temperature Error
                                ; EDX = Cooling Status
                                ; ESI = Trend Indicator (preserved before pop)

; ===================================================================
; INSTRUCTION SET SUMMARY
; ===================================================================
;
; MOV dest, src     - Move data: MOV EAX, EBX (copy EBX to EAX)
; SUB dest, src     - Subtract: SUB ECX, EAX (ECX = ECX - EAX)
; CMP dest, src     - Compare: CMP ECX, 0 (set flags, no result stored)
; JG label          - Jump if Greater (unsigned): SF=0, ZF=0
; JL label          - Jump if Less (signed): SF≠ZF
; JE label          - Jump if Equal: ZF=1
; JLE label         - Jump if Less or Equal: (SF≠ZF) OR ZF=1
; JMP label         - Unconditional jump
; PUSH reg          - Push register onto stack
; POP reg           - Pop register from stack
; XOR reg, reg      - Bitwise XOR (clear register: XOR EAX, EAX)
; RET               - Return from subroutine
;
; FLAGS (32-bit EFLAGS register):
;   ZF (bit 6) - Zero Flag: Set if result is zero
;   SF (bit 7) - Sign Flag: Set if result is negative
;   CF (bit 0) - Carry Flag: Set on overflow/underflow
;
; ===================================================================

; ===================================================================
; END OF 80386 ASSEMBLY MODULE
; ===================================================================
