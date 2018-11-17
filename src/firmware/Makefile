CROSS = riscv32-unknown-elf

CC = $(CROSS)-gcc
AS = $(CROSS)-as
LD = $(CROSS)-ld
OC = $(CROSS)-objcopy
OD = $(CROSS)-objdump

CFLAGS = -Wall # -O3 -fomit-frame-pointer -march=rv32i
AFLAGS = 
LFLAGS = -Ttext=0 -Map=$(PROJ).map
OFLAGS = -O binary
DFLAGS = -d

PROJ = vamps
OBJS = boot.o hello.o

all:
	make $(PROJ).hex

clean:
	-rm *.s *.o $(PROJ).{bin,hex,lst,map}

%.o: %.s
	$(CC) $(CFLAGS) -c $< -o $@

$(PROJ).bin: $(PROJ).o
	$(OC) $(OFLAGS) $< $@
	$(OD) $(DFLAGS) $< > $(PROJ).lst

$(PROJ).hex: $(PROJ).bin
	hexdump -ve '1/4 "%08x\n"' $< > $@
