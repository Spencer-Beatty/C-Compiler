struct a {
    int i;
    int j;
};

struct a b;

void main(){
    struct a c;
    b.i = 10;
    b.j = 20;
    c = b;
    b.i = 0;
    print_i(c.i + c.j);

}