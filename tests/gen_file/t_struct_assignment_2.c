struct a {
    int i;
    int j;
};

void main(){
    struct a b;
    struct a c;
    b.i = 10;
    b.j = 20;
    c = b;
    print_i(c.i + c.j);

}