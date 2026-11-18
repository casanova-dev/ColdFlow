; =========================================================
; COLDFLOW - COA CO4 IMPLEMENTATION
; 64-BIT NASM ASSEMBLY
;
; Demonstrates:
; 1. Temperature Control
; 2. FEFO Inventory Selection
; 3. Warehouse Route Calculation
; 4. Control Signal Generation
; 5. Five Stage Instruction Pipeline
;
; Platform: Linux x86-64
; Calling Convention: System V AMD64 ABI
; Build: nasm -f elf64 co4.asm -o co4.o
;       gcc -no-pie co4.o -o co4
; =========================================================

BITS 64
default rel

global main
extern printf

section .data

    title db 10,"===== COLDFLOW COA CO4 SIMULATOR =====",10,0

    temp_title db 10,"1. TEMPERATURE CONTROL",10,0
    temp_result db "Temperature Error: %d | Cooling: %s",10,0
    cooling_on db "ON",0
    cooling_idle db "IDLE",0
    cooling_off db "OFF",0

    fefo_title db 10,"2. FEFO INVENTORY SELECTION",10,0
    fefo_result db "Selected Batch: %s (Earliest Expiry)",10,0
    batch_a db "BATCH_A",0
    batch_b db "BATCH_B",0

    route_title db 10,"3. WAREHOUSE ROUTE CALCULATION",10,0
    route_result db "Total Robot Distance: %d | Status: %s",10,0
    route_ok db "ACCEPTABLE",0
    route_high db "REVIEW REQUIRED",0

    control_title db 10,10,"4. CONTROL UNIT SIGNAL GENERATION",10,0
    control1 db "MOV  : Register Load -> RegWrite=1, MemRead=1",10,0
    control2 db "SUB  : Arithmetic -> ALUOp=SUB, FlagWrite=1",10,0
    control3 db "CMP  : Compare -> ALUOp=SUB, FlagWrite=1",10,0
    control4 db "JCC  : Conditional Branch -> Branch=1",10,0
    control5 db "ADD  : Addition -> ALUOp=ADD, RegWrite=1",10,0

    pipeline_title db 10,"5. FIVE-STAGE INSTRUCTION PIPELINE",10,0
    pipeline_header db "Cycle | IF | ID | EX | MEM | WB",10,0
    p1 db "  1   | I1 | -- | -- | --  | --",10,0
    p2 db "  2   | I2 | I1 | -- | -- | --",10,0
    p3 db "  3   | I3 | I2 | I1 | -- | --",10,0
    p4 db "  4   | I4 | I3 | I2 | I1 | --",10,0
    p5 db "  5   | I5 | I4 | I3 | I2 | I1",10,0
    p6 db "  6   | -- | I5 | I4 | I3 | I2",10,0
    p7 db "  7   | -- | -- | I5 | I4 | I3",10,0
    p8 db "  8   | -- | -- | -- | I5 | I4",10,0
    p9 db "  9   | -- | -- | -- | -- | I5",10,0

    finish db 10,"===== CO4 EXECUTION COMPLETED =====",10,0

section .bss

    temp_error resd 1
    temp_status resq 1
    selected_batch resq 1
    total_distance resd 1
    route_status resq 1

section .text

main:
    ; Preserve callee-saved RBX and keep the stack 16-byte aligned
    ; before every call to printf (System V AMD64 ABI).
    push rbp
    mov rbp,rsp
    push rbx
    sub rsp,8

    lea rdi,[rel title]
    xor eax,eax
    call printf

; =========================================================
; OPERATION 1: TEMPERATURE CONTROL
; =========================================================

    lea rdi,[rel temp_title]
    xor eax,eax
    call printf

    ; Target = -200 (-20.0 C); Current = -185 (-18.5 C)
    mov eax,-185
    mov ebx,-200

    ; Error = Current - Target
    sub eax,ebx
    mov [rel temp_error],eax

    ; Select cooling state from the signed error thresholds.
    cmp eax,20
    jg .cooling_on

    cmp eax,5
    jle .cooling_off

    jmp .cooling_idle

.cooling_on:
    lea rax,[rel cooling_on]
    mov [rel temp_status],rax
    jmp .temp_done

.cooling_idle:
    lea rax,[rel cooling_idle]
    mov [rel temp_status],rax
    jmp .temp_done

.cooling_off:
    lea rax,[rel cooling_off]
    mov [rel temp_status],rax

.temp_done:
    mov esi,[rel temp_error]
    mov rdx,[rel temp_status]
    lea rdi,[rel temp_result]
    xor eax,eax
    call printf

; =========================================================
; OPERATION 2: FEFO INVENTORY SELECTION
; =========================================================

    lea rdi,[rel fefo_title]
    xor eax,eax
    call printf

    ; Expiry dates in YYYYMMDD format:
    ; Batch A = 20261120, Batch B = 20261119.
    mov eax,20261120
    mov ebx,20261119
    cmp eax,ebx

    ; Select the earlier expiry date.
    jle .select_a

    lea rax,[rel batch_b]
    mov [rel selected_batch],rax
    jmp .fefo_done

.select_a:
    lea rax,[rel batch_a]
    mov [rel selected_batch],rax

.fefo_done:
    mov rsi,[rel selected_batch]
    lea rdi,[rel fefo_result]
    xor eax,eax
    call printf

; =========================================================
; OPERATION 3: WAREHOUSE ROBOT ROUTE CALCULATION
; =========================================================

    lea rdi,[rel route_title]
    xor eax,eax
    call printf

    ; Dock to Rack A = 12; Rack A to Rack B = 8; Rack B to Exit = 5.
    mov eax,12
    add eax,8
    add eax,5
    mov [rel total_distance],eax

    ; Compare total distance with limit = 20.
    cmp eax,20
    jle .route_acceptable

    lea rax,[rel route_high]
    mov [rel route_status],rax
    jmp .route_done

.route_acceptable:
    lea rax,[rel route_ok]
    mov [rel route_status],rax

.route_done:
    mov esi,[rel total_distance]
    mov rdx,[rel route_status]
    lea rdi,[rel route_result]
    xor eax,eax
    call printf

; =========================================================
; OPERATION 4: CONTROL UNIT SIGNAL GENERATION
; =========================================================

    lea rdi,[rel control_title]
    xor eax,eax
    call printf

    lea rdi,[rel control1]
    xor eax,eax
    call printf

    lea rdi,[rel control2]
    xor eax,eax
    call printf

    lea rdi,[rel control3]
    xor eax,eax
    call printf

    lea rdi,[rel control4]
    xor eax,eax
    call printf

    lea rdi,[rel control5]
    xor eax,eax
    call printf

; =========================================================
; OPERATION 5: INSTRUCTION PIPELINE DEMONSTRATION
;
; IF = Instruction Fetch; ID = Instruction Decode;
; EX = Execute; MEM = Memory Access; WB = Write Back.
; Illustrative schedule for independent instructions.
; =========================================================

    lea rdi,[rel pipeline_title]
    xor eax,eax
    call printf

    lea rdi,[rel pipeline_header]
    xor eax,eax
    call printf

    lea rdi,[rel p1]
    xor eax,eax
    call printf

    lea rdi,[rel p2]
    xor eax,eax
    call printf

    lea rdi,[rel p3]
    xor eax,eax
    call printf

    lea rdi,[rel p4]
    xor eax,eax
    call printf

    lea rdi,[rel p5]
    xor eax,eax
    call printf

    lea rdi,[rel p6]
    xor eax,eax
    call printf

    lea rdi,[rel p7]
    xor eax,eax
    call printf

    lea rdi,[rel p8]
    xor eax,eax
    call printf

    lea rdi,[rel p9]
    xor eax,eax
    call printf

; =========================================================
; PROGRAM EXIT
; =========================================================

    lea rdi,[rel finish]
    xor eax,eax
    call printf

    xor eax,eax
    add rsp,8
    pop rbx
    pop rbp
    ret
