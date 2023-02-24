struct struct1 {
    int x;
};

struct struct2 {
    struct struct1 arr[5];
};

void foo() {
    struct struct2 test;
    test.arr[3].x = 3;
}