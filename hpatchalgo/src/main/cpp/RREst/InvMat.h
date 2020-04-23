#include <stdlib.h>
#include <math.h>
#include <stdio.h>


typedef struct _mat
{
	int rows;
	int cols;
	double **data;
} mat;

void InvMat(mat *a, mat *b);
void SetZeroMat(mat *m);
int IsZero(mat *m);
void InitMat(mat *m, int rows, int cols);
void FinalizeMat(mat *m);
void SetMat(mat *m, int row, int col, double value);

void CopyMat(mat *src, mat *dst);
void AddMat(mat *a, mat *b, mat *c);
void SubMat(mat *a, mat *b, mat *c);
void ScaleMat(mat *a, double scale, mat *c);
void MulMat(mat *a, mat *b, mat *c);

void Separate4Mat(mat *a, mat *a11, mat *a12, mat *a21, mat *a22);
void Combine4Mat(mat *a, mat *a11, mat *a12, mat *a21, mat *a22);

void InvMat(mat *a, mat *inv_a);
void TransposeMat(mat *a, mat *transpose_a);