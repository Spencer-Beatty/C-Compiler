struct a {
    int i;
    int j;
};
struct a b;

void main(){

    struct a c;
    b.i = 10;
    b.j = 20;
    c.i = 2;
    c.j = b.j;
    print_i(c.i + c.j);

}