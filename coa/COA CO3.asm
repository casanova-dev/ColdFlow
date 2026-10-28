; ColdFlow COA CO3 - x86-64 arithmetic demonstration
; NASM syntax, 64-bit Linux System V AMD64 ABI.
; Arguments: RDI, RSI, RDX, RCX, R8, R9. Return value in RAX.
; Arithmetic core is implemented in x86-64 assembly.
; ADD/SUB EFLAGS are captured immediately after the instruction.
; MUL defines CF/OF based on whether the high half is nonzero; other flags
; after MUL are undefined. DIV/IDIV leave arithmetic flags undefined.

BITS 64
DEFAULT REL
SECTION .text
global cf_add_u64
global cf_sub_u64
global cf_mul_u64
global cf_div_u64
global cf_idiv_s64
global cf_shift_add_u64

; struct AddSubResult { uint64_t result; uint64_t rflags; };
; void cf_add_u64(uint64_t a, uint64_t b, AddSubResult *out)
cf_add_u64:
    mov r8, rdx
    mov rax, rdi
    add rax, rsi
    pushfq
    pop rcx
    mov [r8], rax
    mov [r8+8], rcx
    ret

; CF=1 after SUB means unsigned borrow; OF means signed overflow.
cf_sub_u64:
    mov r8, rdx
    mov rax, rdi
    sub rax, rsi
    pushfq
    pop rcx
    mov [r8], rax
    mov [r8+8], rcx
    ret

; struct MulResult { uint64_t low; uint64_t high; uint64_t rflags; };
; void cf_mul_u64(uint64_t a, uint64_t b, MulResult *out)
; Unsigned product is RDX:RAX.
cf_mul_u64:
    mov r8, rdx
    mov rax, rdi
    mul rsi
    pushfq
    pop rcx
    mov [r8], rax
    mov [r8+8], rdx
    mov [r8+16], rcx
    ret

; struct DivResult { uint64_t quotient; uint64_t remainder; uint64_t status; };
; int cf_div_u64(uint64_t dividend, uint64_t divisor, DivResult *out)
; status 0=success, 1=zero divisor.
cf_div_u64:
    mov r8, rdx
    test rsi, rsi
    jz .udiv_zero
    mov rax, rdi
    xor rdx, rdx
    div rsi
    mov [r8], rax
    mov [r8+8], rdx
    mov qword [r8+16], 0
    xor eax, eax
    ret
.udiv_zero:
    mov qword [r8], 0
    mov qword [r8+8], 0
    mov qword [r8+16], 1
    mov eax, 1
    ret

; int cf_idiv_s64(int64_t dividend, int64_t divisor, DivResult *out)
; status 0=success, 1=zero divisor, 2=INT64_MIN / -1 overflow.
cf_idiv_s64:
    mov r8, rdx
    test rsi, rsi
    jz .sdiv_zero
    mov rax, rdi
    mov rcx, 0x8000000000000000
    cmp rax, rcx
    jne .sdiv_do
    cmp rsi, -1
    je .sdiv_overflow
.sdiv_do:
    cqo
    idiv rsi
    mov [r8], rax
    mov [r8+8], rdx
    mov qword [r8+16], 0
    xor eax, eax
    ret
.sdiv_zero:
    mov qword [r8], 0
    mov qword [r8+8], 0
    mov qword [r8+16], 1
    mov eax, 1
    ret
.sdiv_overflow:
    mov qword [r8], 0
    mov qword [r8+8], 0
    mov qword [r8+16], 2
    mov eax, 2
    ret

; void cf_shift_add_u64(uint64_t a, uint64_t b, MulResult *out)
; 64-iteration software shift-and-add multiplication, returning a 128-bit product.
; R8:R9 is the shifting multiplicand; R10:R11 is the accumulator.
cf_shift_add_u64:
    push rbx
    push r12
    push r13
    mov r12, rdx            ; output pointer (callee-saved register)
    mov r8, rdi             ; multiplicand low
    xor r9, r9              ; multiplicand high
    mov rbx, rsi             ; multiplier
    xor r10, r10             ; accumulator high
    xor r11, r11             ; accumulator low
    mov ecx, 64
.sa_loop:
    test rbx, 1
    jz .sa_skip
    add r11, r8
    adc r10, r9
.sa_skip:
    shld r9, r8, 1
    shl r8, 1
    shr rbx, 1
    dec ecx
    jnz .sa_loop
    mov [r12], r11
    mov [r12+8], r10
    mov qword [r12+16], 0   ; no MUL flags exist for this software algorithm
    pop r13
    pop r12
    pop rbx
    ret
