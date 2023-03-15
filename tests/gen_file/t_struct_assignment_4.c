struct a {
    int i;
    int j;
};

void main(){
    struct a b;
    struct a c;
    b.i = 1;
    b.j = 2;
    c = b;
    print_i(c.i + c.j);
}