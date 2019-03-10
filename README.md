# Vamps
Hoppla getz foila un processeur avec Chisel et Lucienne.

## RTL Tests

To launch test with treadle do :
sbt 'test:runMain vamps.VampsMain --generate-vcd-output on'

With verilator, and generate verilog sources:
sbt 'test:runMain vamps.VampsMain --backend-name verilator'

### Waves

Waveforms are available in following directories :

```
./test_run_dir/vamps.VampsMain34873972/Vamps.vcd
./test_run_dir/vamps.MiVampsMain450095880/MiVamps.vcd
```

And could be vizualized with gtkwave.

A gtkwave configuration file named config_gtkwave.gtkw is available in project. And could be used as following :

```bash
$ gtkwave -a config_gtkwave.gtkw
```

## Firmware

Assembly program vamps.s is available in directory src/firmware/. The hex file could be generated using make :

```bash
$ cd src/firmware
$ make
```

## Libero MiV Creative Board 

To generate verilog for MiV Creative Board do
sbt 'test:runMain vamps.MiVampsMain --backend-name verilator'

To test it :
sbt 'test:runMain vamps.MiVampsMain --generate-vcd-output on'

```bash
$ sbt 'test:runMain vamps.VampsMain --generate-vcd-output on'
```

With verilator, and generate verilog sources:
```bash
$ sbt 'test:runMain vamps.VampsMain --backend-name verilator'
```
