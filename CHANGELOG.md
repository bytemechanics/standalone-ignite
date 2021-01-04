# Version 2.0.0 (2020/01/04)
---

## Changes
* Automatic Standalone instance to IgnitableAdapter instances
* Change standalone console attribute from OutConsole to the interface


# Version 2.0.0 (2021/01/01)
---

## Changes
* Fixed some possible problems in exceptions due null parameters
* Added unit tests for exceptions
* Added utility abstract class IgnitableAdapter in order to reduce repeated code
* Added utility method in Ignitable interface to recover Console
* Added utility method in Ignitable to recover the original Standalone instance
* Added an additional description to the Standalone class in order to improve help messages
* Added an console and consoleFormatter to Standalone builder in order to allow change console class and console formatter
* Changed Standalone builder() method to add mandatory parameter Ignitable in order to clarify that's mandatory
* Console abstracted to an interface
* Added shell utility classes


# Version 1.2.0 (2020/11/30)
---

## Changes
* Move from Spock test framework to Junit5+JMockit
* Add apache felix to point the public and private packages

## Features
* #38 Add flag to disable banner
* #36 Parameter should have a get method to get the value without Optional usage
* #14 Make enum parameter parser able to parse non-case sensitive value 
* #34 If Ignitable implements Autocloseable Standalone should invoke close 
* #37 Console abstraction to log startup messages 
* #35 Parameter interface should have a validate method to override by semantic validations 