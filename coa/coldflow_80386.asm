section .data

    IDLE_THRESHOLD      equ 5
    COOLING_THRESHOLD   equ 20
    MEDIUM_THRESHOLD    equ 30
    HIGH_THRESHOLD      equ 40

section .text

    global coldflow_temp_control

coldflow_temp_control:

    mov rcx, rbx
    sub rcx, rax

    mov rdx, 0
    mov r8,  0
    mov r9,  0

    cmp rcx, HIGH_THRESHOLD
    jg critical_cooling

    cmp rcx, MEDIUM_THRESHOLD
    jg medium_cooling

    cmp rcx, COOLING_THRESHOLD
    jg low_cooling

    cmp rcx, IDLE_THRESHOLD
    jg idle_mode

    jmp cooling_off

critical_cooling:

    mov rdx, 2
    mov r8,  3
    mov r9,  1
    jmp finish

medium_cooling:

    mov rdx, 2
    mov r8,  2
    jmp finish

low_cooling:

    mov rdx, 2
    mov r8,  1
    jmp finish

idle_mode:

    mov rdx, 1
    mov r8,  0
    jmp finish

cooling_off:

    mov rdx, 0
    mov r8,  0
    mov r9,  0

finish:

    ret