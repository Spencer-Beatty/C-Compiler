struct a {
    int i;
};

struct b {
    struct a a;
};

void main(){
    struct a a;
    struct b b;
    b.a = a;
}
