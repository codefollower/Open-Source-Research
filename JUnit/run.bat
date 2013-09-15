@echo off
rem java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.SimpleTest
rem java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.SimpleTest org.junit.samples.SimpleTest2 > myout.java 2>&1


rem java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.IgnoreTest  org.junit.samples.RunWithTest org.junit.samples.Pre4Test org.junit.samples.JUnit4ClassRunnerTest org.junit.samples.ListTest org.junit.samples.SimpleTest > myout.java 2>&1

rem java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.JUnit4ClassRunnerTest > myout.java 2>&1

rem java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.JUnit4ClassRunnerTest > myout.java


rem java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.SimpleTest org.junit.my org.junit.samples.Pre4Test > myout.java


rem java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.IgnoreTest > myout.java

rem java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.SimpleTest > myout.java

rem java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.JUnit4ClassRunnerTest > myout.java

java -cp src;classes;classes\test org.junit.runner.JUnitCore org.junit.samples.AssertTest
