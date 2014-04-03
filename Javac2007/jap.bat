@echo off
javap -classpath .;classes -verbose test.jvm.GenTest >javap.txt

rem javap -classpath .;classes -verbose test.jvm.LoadClassTest >javap.txt
