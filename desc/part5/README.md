# Part V: Object-Oriented Features

**Important:** These instructions are subject to change.
Please make sure to check any updates made to this page (use the "watch" feature on GitLab to be automatically notified).


The goal of part V is to extend your compiler to support a subset of object-oriented features based on a combination of Java and C++ with.
The functionalities you are supposed to support in your mini-compiler consist of:



### 1. Class Definition: 

A Class is a user-defined data type with data members, called fields, and member functions.
Each class creation must be done using the `class` keyword.
To avoid complexity, all methods and fields are public in a class.
We also assume that there is a unique implicit constructor for each class automatically defined.
The implicit constructor takes no arguments and simply allocate space for storing the data corresponding the instantiated object.
The following code is an example of the class definition:


```C
class Course {
    char name[20];
    int credit;
    int courseWorkScore;

    void whereToAttend(){
        print_s((char*)"Not determined! The course will be held virtually or in person!\n");
    }
    int hasExam(){
        if(courseWorkScore == 100)
            return 0;
        else
            return 1;
    }
}
```

### 2. Class Instantiation:

No memory is allocated upon the definition of a class.
Classes are like blueprints for objects, meaning that only when instantiating a class, memory is allocated to its object.
Once a class is declared, you can instantiate it anywhere in your code.
Instantiation should be done using the `new` keyword followed by a `class` keyword.
An example of class `Course` instantiation can be found below: 


```C
void main(){
    class Course comp520;
    comp520 = new class Course();
}
```
You can see that even though there is no constructor defined in the `Course` body, the object `comp520` is instantiated using the implicit default class constructor.

### 3. Field Access and Assignment:

Since class fields are public, they can be accessed directly, meaning there is no need to provide "Encapsulation" in this project.
So, this piece of code is valid and can be used to access the class's fields and alter their values:

```C
int credit;
credit = comp520.credit; // field access


comp520.credit = 4; // field assignment
```


### 4. Class Method Call:

Like field access, you can call an object's method wherever you like without any restriction:
```C
if(comp520.hasExam())
    print_s((char*)"Be ready for the exam!\n");
else
    print_s((char*)"Be ready for the project implementation!:)\n");
```

### 5. Class inheritance:

The capability of a class to derive properties and characteristics from another class is called "Inheritance".
By supporting this feature in your compiler, you can reach OOP!
Inheritance is indicated using `extends` followed by the parent's name in the class signature: 
```C
class VirtualCourse extends Course {
    char zoomLink[200];
}
```
Our language will only support single inheritance.
This means that each class can inherit from at most one class.
Please note that we do not support "recursive inheritance" in our mini-compiler because any parent class must be declared fully first to be able to be inherited. 

### 6. Class Passing by Reference:

To simplify the implementation, similar to Jave and unlike C++, we assume that class object variables are simply references that point to real objects in the memory heap. 
This means that `comp520` in the previous example is only the reference to its object, so there is no need to use `&` or `*` for referencing and dereferencing class objects, respectively.
Using these two operators, `&` and `*` only influence the class's reference, not the object.
Also, if you pass a class object to a method, any modification to the object's fields will change the actual object's field values.

Because of this feature, some functions operations need to be reconsidered:

#### 6.1. EQ/NEQ Binary Expression: 

Class objects are passed by references in our language, meaning objects are pointers to their location in memory but are of class types in nature.
So, while applying EQ and NEQ on class objects, it suffices to check whether two operands point to the identical location in the memory. 

#### 6.2. SizeOf Operator:

Because of class object reference passing, the `sizeof` operator returns the size of a pointer.

#### 6.3. Assignment Statement: 

In the class assignment, the pointer address of the right-hand-side operand will be assigned to the left-hand-side one. 
Be aware that when assigning between two classes, their type must be equal.
If one class is a subtype of another class, you will have to use a typecast operation since our language is strongly typed.


### 7. Polymorphism:

When you have a hierarchy of classes, and they are related by inheritance, you can have a call to a member function which causes a different function to be executed depending on the type of object that invokes the function.
This feature is called "Polymorphism" and necessitates using a virtual table and dynamic dispatch.
Your compiler must support polymorphism.
Consider the following example:
```C
class VirtualCourse extends Course {
    int isOnZoom;
    
    void whereToAttend(){
        print_s((char*)"The course is going to be held on Zoom!\n")
    }
}

int main() {
    class Course course;
    course = new class Course();
    class VirtualCourse vcourse;
    vcourse = new class VirtualCourse();

    course.whereToAttend();
    vcourse.whereToAttend();
    return 0;
}
```
In the above example, `VirtualCourse` overrides the `whereToAttend` method of `Course`; So, the code produces the below output:

```C
Not determined! The course will be held virtually or in person!
The course is going to be held on Zoom!
```
Note that only functions can be overridden, and it is forbidden to override a field in a subclass if it already exists in an ancestor class.

### 8. Typecasting and Subtyping:

Our compiler support typecasting, in which you can assign an ancestor's type to its descendants' class objects.
This feature is called "Upcasting".
We do not support "Downcasting", and you cannot assign a descendant's class type to its ancestors.
To avoid any ambiguity in the grammar between parenthesis for arithmetic operations and class typecasting, you should use the `class` keyword followed by the class name inside the parenthesis for typecasting.
Below, you can see an example of class type casting:
```C
course = (class Course) vcourse;
```
Once typecasting is done, you can no longer access fields and methods declared in the original child class.
You can only call methods, and access fields declared in the newly-assigned class type and its ancestors.
In this piece of code, the first statement is valid while the second is not.
```C
course.credit = 4; //Valid
course.isOnZoom = 1; //Invalid
```
Subtyping refers to the compatibility of the types.
In general in OOP languages, if an expression of type class A is expected, and you provided an expression of type class B, where class B is a subtype of class A (i.e. class B directly or indirectly inherit from class A), this is valid.
However, since our language is strongly typed, you have to explicit cast between subtypes.
For example, if a function receives A as an argument and you want to pass B object to it, you have to use the typecast operator `(A) b` explicitly and pass the object to the function.


To support these functionalities, you need to make some modifications to some parts you have implemented so far.
Here is a summary of the changes you need to made followed by more details:

1. **Lexer:** Add new tokens (CLASS, NEW, EXTENDS) and update your tokenizer.
2. **Parser:** Make sure that the input program complies with the newly added grammar rules.
3. **Name Analyzer:** Add new symbols for class declaration and instantiation and modify symbol tables.
4. **Type Checker:** Make sure that accessed fields and called functions exist in the class or its ancestors, ensuring newly added typing rules are followed, supporting class typecasting.
5. **Code Generator:** Create virtual tables, implement dynamic dispatch, build object layout, and pass implicit pointers to class function calls.
6. **Register Allocator:** Nothing to change.

Please note that there is no partial implementation provided for this phase.
You have to modify your current compiler based on the description, and updated grammars and typing-rules.

## 0. Our Language!


Since the C compiler is not designed to support object-oriented programming (OOP), these features are borrowed from C++ and Java. These web tutorials are a good starting point if you are not familiar with [C++](https://www.cplusplus.com/doc/tutorial/) or [Java](https://www.javatpoint.com/java-oops-concepts).
You do not have to read all of them since a subset of OOP features are considered for this project and some features, like reference passing, are not entirely compatible with either C++ or Java.
Pay special attention to features described earlier or provided in the EBNF grammar.

Note that there is no standard compiler available to support the syntax and features we considered in this phase.
However, you can check the output of your code in C++ or Java standard compilers by altering the input program to meet our language definition.
This code modification might be a little tricky, but it is a good practice for you to understand the differences between two languages and our modified language.

## 1. Lexing

You have to introduce new tokens `CLASS`, `EXTENDS`, and `NEW` to tokenize the `class`, `extends`, and `new` keywords, respectively.
Then, you have to alter the next() method to detect newly introduced keywords.
Note again that you are **not** allowed to use [the Java regular expression matcher](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Matcher.html) in your implementation!

## 2. Grammar 

To support OOP in your compiler, you have to add additional rules to your grammar.
You can see these changes in newly-updated EBNF grammar.
Any new modification is indicated with the comment `#part V` in the grammar file.
Some important notes regarding EBNF grammar are listed below:

- **Class Declaration:** This rule indicates a class definition.
- **Class Function Call:** This rule is dedicated to the class function call. 
- **Class Field Access:** Notably, class field access is similar to structure field member access. So, there is no need to change the grammar file, and you have to take care of it later.
- **Class Type:** The updated grammar has a new type named `classtype,` an identifier indicating a declared class in the program.
- **Class Instance:** This rule is used to produce an expression for instantiating a new class object.

Your next job is transforming the updated grammar into an equivalent context-free LL(k) grammar like what you did in part I. Do not forget to ensure that you eliminate left-recursion from the grammar and, if needed, left-factorize it. Since the changes to the grammar are relative few, you should be able to patch put your existing massaged grammar without restarting from scratch.

## 3. Parser

In this step, you should extend your `Parser`-class to determine object-oriented programmed source code is syntactically correct. Like before, make sure you use the `error`-method in the class to report errors correctly!

## 4. Operator precedence and associativity

`.` operator has the precedence of 1 and is dedicated to class/struct field access and class function call. In the updated grammar, eliminate left recursion, and like phase II, ensure that dot,`.`, operator precedence is respected for newly-added object-oriented features. Again you can start from your already massaged grammar and make slight changes rather than starting from scratch.

The `new` operator has the same precedence level as the unary `+` operator: precedence level is 2 with Right-to-left associativity.

## 5. AST Nodes

In this step, you should add some AST nodes to your compiler based on the newly-updated provided abstract grammar file. Like before, ensure that the design of your classes follows the abstract grammar. New rules are indicated using the comment `#Part V` in the [abstract grammar file](./abstract_grammar.txt). Please pay special attention to inheritance while adding new AST nodes. It would be best if you stored the class's parent in its AST node to be able to access it later. 

## 6. Parser Modification

Similar to Part II, update your parser to create and return the newly-added AST nodes while it parses object-oriented-related parts of your input program.

## 7. AST Printer

We will not mark based on the AST printer; however, modifying your AST printer to support OO features would be helpful to verify your AST construction.

## 8. Name Analysis

ِYou have `class` in your compiler, which can have multiple fields and method declarations inside its scope and inherits from another class. The body of a class is similar to a struct's body, in which you cannot have nested blocks or redeclared fields and methods. Class declarations are made in the global scope. Based on this explanation, you should extend your `Symbol` class to have a new symbol called `Class` and update the name analyzer visitor to verify that the input program complies with defined rules. Note that new defined `Class` symbol should contain a pointer to its parent, which can be null if it is not derived from another class. Also, you have to collect all field and method names declared in the class in the similar way you did for structs.

## 9. Type Analysis

Class declarations have been symbolized as `ClassDecl` in the AST earlier. You must make sure each declared class has a unique name in your type analyzer. Then, you must ensure that when facing a class function call, or field access, the function or field exists in the corresponding class declaration or its parents. This note is the essential part of object-oriented programming language type checking, in which you must have to check all ancestors of a class while analyzing field access or function call.

Typing rules are updated based on Class types and can be accessed [here](./typing-rules/rules.pdf). All new or updated rules are written in red. Like before, you are responsible for extending `sem.TypeCheckVisitor` to follow newly-added rules. Please take special care of the necessary directives mentioned below to provide a fully-functioning type-checker.

 The `<:` operator in typing inference rule indicates subtyping, meaning that T<sub>1</sub> corresponding class is a descendant of T<sub>2</sub> corresponding one.


#### Checking lvalues:
Please note that *lvalues* have not been changed in the modified grammar. However, be aware that FieldAccessExpr is now used for both struct and class.


## 10. Virtual Table Creation

Before heading to the code generation part, you have to make virtual tables for your classes' functions to support dynamic dispatch.
To simplify its concept, we break it down into multiple stages to implement:

### 10.1. Label Creation:
For each method in each class, we need a unique label to emit. In this step, you should traverse your class declaration nodes and generate a label for each declared method inside their bodies. 

### 10.2. Virtual Table Creation:
Since the virtual table is shared among all objects instantiated from the same class, we need one per class and a pointer in the object to point to the corresponding "vtable." To do so, traversing all class declaration nodes, do the following steps:

#### Step I
For each class, find all its ancestors recursively.
#### Step II
When you reach an ancestor who does not have any parents, start to collect its methods' labels in the order of declaration. Consider a map of the function name to the function label to simplify the label collection. Maintaining the order of elements in the virtual table when inserting is vital; you can use a `LinkedHashMap` for instance in Java to maintain a fixed position for all methods.
#### Step III
Then, traverse the subtree downward to reach the original class declaration. While traversing, append each new declaration to your method label map with its corresponding label. Because we should call the most-recently-overridden function for a class, if you visit a previously-declared method, you should update its element to the newly-visited method's label. Stop this process whenever you visit all the declared functions in the corresponding class declaration and its ancestors.
#### Step IV
Now, you have your virtual table for your class. Treat these tables like a globally-declared variable and declare them after the global variable declaration in the assembly program. 
#### Step V
Each class object must have a pointer to this table's first element. To do so, you can add a field to the `ClassType` AST node, which stores the address of the first element of the virtual table. Doing so, upon each class function call, like what was mentioned in the lectures, you should first retrieve the class object's virtual table pointer, then get the label (address) of the corresponding function for a subroutine call. Finally, `jump&link` to the subroutine.


## 11. Code Generation

After creating virtual tables and setting pointers in their corresponding class objects, you should generate code for object-oriented-related parts in the input program. The trickiest part of this process is dedicated to dynamic dispatch, which was explained in the previous section. Now, we should take care of class variables in programs. For each ClassType variable, we have a struct-like data type with an array-like pointer. You should follow the policy of storing structures but treat the variable similar to an array and pass its pointer to functions. Doing so, executing operations like `class assignment`, `EQ/NEQ`, `sizeof` will be straightforward. 

**Important**: When a function is declared inside a class, it may access the class's fields and change their values. In doing so, the function needs a pointer to its corresponding object layout while it is being called to know the exact location of fields. So, You must pass the object class, which is actually a pointer, to the object's function when called. To simplify your implementation, while generating code for class functions calls, always pass its corresponding object pointer as an implicit first argument to the function.

**Tip**:, you could write a pass which modifies your AST to add the reference to the object to every function call on an object, and then use this reference to access the field of your class within the function. This will keep the logic of your code generator simpler and lead to a cleaner design.

To produce code for class object variables, first, we have to create the object layout for them with a similar procedure we did for method dispatch. As mentioned in lectures, every field x is at the same offset from the start of the object in one arbitrary class and its parent! So, with a minor modification, you have to implement label creation and virtual tables supporting (steps 1 to 3) for objects' fields. The difference is that we do not have redeclared fields in class inheritance. So, you must append any field declaration you see and its label to the map while traversing the tree downward.

You will have an object layout containing all declared fields' labels in the object's body and its ancestors' with the same offset for each object class. Then, you only need to pass the pointer to the first field of this object as `instance ptr`. Whenever a field value modification is done, you retrieve the corresponding field address from the object layout based on its pointer and update the field's value. 

## 12. Register Allocation

If you reach this step, it means you are done! **Congratulations!**
There is no need to change your register allocator!

Tap on your back for your effort, and enjoy your newly augmented compiler! :)
