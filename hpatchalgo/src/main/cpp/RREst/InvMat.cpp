#include "InvMat.h"

void SetZeroMat(mat *m)
{
	int row, col;
	for (row = 0; row < m->rows; row++)
	{
		for (col = 0; col < m->cols; col++)
		{
			m->data[row][col] = 0;
		}
	}
}

int IsZero(mat *m)
{
	int row, col;
	int n_row = m->rows;
	int n_col = m->cols;
	double **p = m->data;
	for (row = 0; row < n_row; row++)
	{
		for (col = 0; col < n_col; col++)
		{
			if (p[row][col] != 0)
				return 0;
		}
	}

	return 1;
}

void InitMat(mat *m, int rows, int cols)
{
	int i;

	m->rows = rows;
	m->cols = cols;
	m->data = (double**)calloc(rows, sizeof(double*));
	for (i = 0; i < rows; i++)
	{
		m->data[i] = (double*)calloc(cols, sizeof(double));
	}

}

void FinalizeMat(mat *m)
{
	int i;
	for (i = 0; i < m->rows; i++)
	{
		free(m->data[i]);
	}
	free(m->data);
}

void SetMat(mat *m, int row, int col, double value)
{
	m->data[row][col] = value;
}

void CopyMat(mat *src, mat *dst)
{
	int row, col;
	for (row = 0; row < src->rows; row++)
	{
		for (col = 0; col < src->cols; col++)
		{
			dst->data[row][col] = src->data[row][col];
		}
	}
}

void AddMat(mat *a, mat *b, mat *c)
{
	int row, col;
	for (row = 0; row < a->rows; row++)
	{
		for (col = 0; col < a->cols; col++)
		{
			c->data[row][col] = a->data[row][col] + b->data[row][col];
		}
	}
}

void SubMat(mat *a, mat *b, mat *c)
{
	int row, col;
	for (row = 0; row < a->rows; row++)
	{
		for (col = 0; col < a->cols; col++)
		{
			c->data[row][col] = a->data[row][col] - b->data[row][col];
		}
	}
}

void ScaleMat(mat *a, double scale, mat *c)
{
	int row, col;
	for (row = 0; row < a->rows; row++)
	{
		for (col = 0; col < a->cols; col++)
		{
			c->data[row][col] = scale * a->data[row][col];
		}
	}
}

void MulMat(mat *a, mat *b, mat *c)
{
	// (m x n) * (n x k) = (m x k)
	int m, n, k;
	for (m = 0; m < a->rows; m++)
	{
		for (k = 0; k < b->cols; k++)
		{
			c->data[m][k] = 0;
			for (n = 0; n < a->cols; n++)
			{
				c->data[m][k] += (a->data[m][n] * b->data[n][k]);
			}
		}
	}
}

void Separate4Mat(mat *a, mat *a11, mat *a12, mat *a21, mat *a22)
{
	int n = a->rows;
	int hn = n / 2;
	int i, j;

	// a11
	for (i = 0; i < hn; i++) {
		for (j = 0; j < hn; j++) {
			a11->data[i][j] = a->data[i][j];
		}
	}
	// a12
	for (i = 0; i < hn; i++) {
		for (j = hn; j < n; j++) {
			a12->data[i][j - hn] = a->data[i][j];
		}
	}
	// a21
	for (i = hn; i < n; i++) {
		for (j = 0; j < hn; j++) {
			a21->data[i - hn][j] = a->data[i][j];
		}
	}
	// a22
	for (i = hn; i < n; i++) {
		for (j = hn; j < n; j++) {
			a22->data[i - hn][j - hn] = a->data[i][j];
		}
	}
}

void Combine4Mat(mat *a, mat *a11, mat *a12, mat *a21, mat *a22)
{
	int n = a->rows;
	int hn = n / 2;
	int i, j;

	// a11
	for (i = 0; i < hn; i++) {
		for (j = 0; j < hn; j++) {
			a->data[i][j] = a11->data[i][j];
		}
	}
	// a12
	for (i = 0; i < hn; i++) {
		for (j = hn; j < n; j++) {
			a->data[i][j] = a12->data[i][j - hn];
		}
	}
	// a21
	for (i = hn; i < n; i++) {
		for (j = 0; j < hn; j++) {
			a->data[i][j] = a21->data[i - hn][j];
		}
	}
	// a22
	for (i = hn; i < n; i++) {
		for (j = hn; j < n; j++) {
			a->data[i][j] = a22->data[i - hn][j - hn];
		}
	}
}

void InvMat(mat *a, mat *inv_a)
{
	if (a->rows == 2)
	{
		double det = (a->data[0][0] * a->data[1][1]) - (a->data[0][1] * a->data[1][0]);
		if (det == 0)
		{
			inv_a->data[0][0] = 0;
			inv_a->data[0][1] = 0;
			inv_a->data[1][0] = 0;
			inv_a->data[1][1] = 0;
		}
		else
		{
			inv_a->data[0][0] = a->data[1][1] / det;
			inv_a->data[1][1] = a->data[0][0] / det;
			inv_a->data[0][1] = -1 * a->data[0][1] / det;
			inv_a->data[1][0] = -1 * a->data[1][0] / det;
		}
	}
	else
	{

		if (IsZero(a)) {
			SetZeroMat(inv_a);
			return;
		}


		int hn = a->rows / 2;
		mat a11, a12, a21, a22;
		mat y11, y12, y21, y22;
		mat inv_a11;
		mat b, inv_b;
		mat tmp1, tmp2;

		// init
		InitMat(&a11, hn, hn);
		InitMat(&a12, hn, hn);
		InitMat(&a21, hn, hn);
		InitMat(&a22, hn, hn);
		InitMat(&y11, hn, hn);
		InitMat(&y12, hn, hn);
		InitMat(&y21, hn, hn);
		InitMat(&y22, hn, hn);
		InitMat(&inv_a11, hn, hn);
		InitMat(&b, hn, hn);
		InitMat(&inv_b, hn, hn);
		InitMat(&tmp1, hn, hn);
		InitMat(&tmp2, hn, hn);

		// separate
		Separate4Mat(a, &a11, &a12, &a21, &a22);

		// inv(a11)
		InvMat(&a11, &inv_a11);
		// b = a22 - a21*inv(a11)*a12
		//   = a22 - tmp1 * a12
		//   = a22 - tmp2
		MulMat(&a21, &inv_a11, &tmp1);
		MulMat(&tmp1, &a12, &tmp2);
		SubMat(&a22, &tmp2, &b);
		// inv(b)
		InvMat(&b, &inv_b);

		// y11 = inv(a11) + inv(a11) * a12 * inv(b) * a21 * inv(a11)
		//     = inv(a11) +           tmp1 * inv(b) * a21 * inv(a11)
		//     = inv(a11) +                    tmp2 * a21 * inv(a11)
		//     = inv(a11) +                          tmp1 * inv(a11)
		//     = inv(a11) +                                     tmp2
		MulMat(&inv_a11, &a12, &tmp1);
		MulMat(&tmp1, &inv_b, &tmp2);
		MulMat(&tmp2, &a21, &tmp1);
		MulMat(&tmp1, &inv_a11, &tmp2);
		AddMat(&inv_a11, &tmp2, &y11);

		// y12 = -1 * inv(a11) * a12 * inv(b)
		// y12 = -1 *           tmp1 * inv(b)
		// y12 = -1 *                    tmp2
		MulMat(&inv_a11, &a12, &tmp1);
		MulMat(&tmp1, &inv_b, &tmp2);
		ScaleMat(&tmp2, -1, &y12);

		// y21 = -1 * inv(b) * a21 * inv(a11)
		// y21 = -1 *         tmp1 * inv(a11)
		// y21 = -1 *                    tmp2
		MulMat(&inv_b, &a21, &tmp1);
		MulMat(&tmp1, &inv_a11, &tmp2);
		ScaleMat(&tmp2, -1, &y21);

		// y22 = inv(b);
		CopyMat(&inv_b, &y22);

		// combine
		Combine4Mat(inv_a, &y11, &y12, &y21, &y22);

		// finalize
		FinalizeMat(&a11);
		FinalizeMat(&a12);
		FinalizeMat(&a21);
		FinalizeMat(&a22);
		FinalizeMat(&y11);
		FinalizeMat(&y12);
		FinalizeMat(&y21);
		FinalizeMat(&y22);
		FinalizeMat(&inv_a11);
		FinalizeMat(&b);
		FinalizeMat(&inv_b);
		FinalizeMat(&tmp1);
		FinalizeMat(&tmp2);
	}
}

void TransposeMat(mat *a, mat *transpose_a) {
	
	int row, col;
	
	for (row = 0; row < a->rows; row++) {
		for (col = 0; col < a->cols; col++) {
			transpose_a->data[col][row] = a->data[row][col];
		}
	}
}