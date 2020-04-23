#include "lib_matrix.h"

////////////////////////////////////////////////////////////////////////////////
// File: singular_value_decomposition.c                                       //
// Contents:                                                                  //
//    Singular_Value_Decomposition                                            //
//    Singular_Value_Decomposition_Solve                                      //
//    Singular_Value_Decomposition_Inverse                                    //
////////////////////////////////////////////////////////////////////////////////

#include <string.h>              // required for memcpy()
#include <float.h>               // required for FLT_EPSILON
#include <math.h>                // required for fabs(), sqrt();

#define MAX_ITERATION_COUNT 30   // Maximum number of iterations
#ifndef FLT_EPSILON
#define FLT_EPSILON     1.192092896e-07F        /* smallest such that 1.0+FLT_EPSILON != 1.0 */
#endif
//                        Internally Defined Routines 
static void Householders_Reduction_to_Bidiagonal_Form(float* A, int nrows,
	int ncols, float* U, float* V, float* diagonal, float* superdiagonal);
static int  Givens_Reduction_to_Diagonal_Form(int nrows, int ncols,
	float* U, float* V, float* diagonal, float* superdiagonal);
static void Sort_by_Decreasing_Singular_Values(int nrows, int ncols,
	float* singular_value, float* U, float* V);

////////////////////////////////////////////////////////////////////////////////
//  int Singular_Value_Decomposition(float* A, int nrows, int ncols,         //
//        float* U, float* singular_values, float* V, float* dummy_array) //
//                                                                            //
//  Description:                                                              //
//     This routine decomposes an m x n matrix A, with m >= n, into a product //
//     of the three matrices U, D, and V', i.e. A = UDV', where U is an m x n //
//     matrix whose columns are orthogonal, D is a n x n diagonal matrix, and //
//     V is an n x n orthogonal matrix.  V' denotes the transpose of V.  If   //
//     m < n, then the procedure may be used for the matrix A'.  The singular //
//     values of A are the diagonal elements of the diagonal matrix D and     //
//     correspond to the positive square roots of the eigenvalues of the      //
//     matrix A'A.                                                            //
//                                                                            //
//     This procedure programmed here is based on the method of Golub and     //
//     Reinsch as given on pages 134 - 151 of the "Handbook for Automatic     //
//     Computation vol II - Linear Algebra" edited by Wilkinson and Reinsch   //
//     and published by Springer-Verlag, 1971.                                //
//                                                                            //
//     The Golub and Reinsch's method for decomposing the matrix A into the   //
//     product U, D, and V' is performed in three stages:                     //
//       Stage 1:  Decompose A into the product of three matrices U1, B, V1'  //
//         A = U1 B V1' where B is a bidiagonal matrix, and U1, and V1 are a  //
//         product of Householder transformations.                            //
//       Stage 2:  Use Given' transformations to reduce the bidiagonal matrix //
//         B into the product of the three matrices U2, D, V2'.  The singular //
//         value decomposition is then UDV'where U = U2 U1 and V' = V1' V2'.  //
//       Stage 3:  Sort the matrix D in decreasing order of the singular      //
//         values and interchange the columns of both U and V to reflect any  //
//         change in the order of the singular values.                        //
//                                                                            //
//     After performing the singular value decomposition for A, call          //
//     Singular_Value_Decomposition to solve the equation Ax = B or call      //
//     Singular_Value_Decomposition_Inverse to calculate the pseudo-inverse   //
//     of A.                                                                  //
//                                                                            //
//  Arguments:                                                                //
//     float* A                                                              //
//        On input, the pointer to the first element of the matrix            //
//        A[nrows][ncols].  The matrix A is unchanged.                        //
//     int nrows                                                              //
//        The number of rows of the matrix A.                                 //
//     int ncols                                                              //
//        The number of columns of the matrix A.                              //
//     float* U                                                              //
//        On input, a pointer to a matrix with the same number of rows and    //
//        columns as the matrix A.  On output, the matrix with mutually       //
//        orthogonal columns which is the left-most factor in the singular    //
//        value decomposition of A.                                           //
//     float* singular_values                                                //
//        On input, a pointer to an array dimensioned to same as the number   //
//        of columns of the matrix A, ncols.  On output, the singular values  //
//        of the matrix A sorted in decreasing order.  This array corresponds //
//        to the diagonal matrix in the singular value decomposition of A.    //
//     float* V                                                              //
//        On input, a pointer to a square matrix with the same number of rows //
//        and columns as the columns of the matrix A, i.e. V[ncols][ncols].   //
//        On output, the orthogonal matrix whose transpose is the right-most  //
//        factor in the singular value decomposition of A.                    //
//     float* dummy_array                                                    //
//        On input, a pointer to an array dimensioned to same as the number   //
//        of columns of the matrix A, ncols.  This array is used to store     //
//        the super-diagonal elements resulting from the Householder reduction//
//        of the matrix A to bidiagonal form.  And as an input to the Given's //
//        procedure to reduce the bidiagonal form to diagonal form.           //
//                                                                            //
//  Return Values:                                                            //
//     0  Success                                                             //
//    -1  Failure - During the Given's reduction of the bidiagonal form to    //
//                  diagonal form the procedure failed to terminate within    //
//                  MAX_ITERATION_COUNT iterations.                           //
//                                                                            //
//  Example:                                                                  //
//     #define M                                                              //
//     #define N                                                              //
//     float A[M][N];                                                        //
//     float U[M][N];                                                        //
//     float V[N][N];                                                        //
//     float singular_values[N];                                             //
//     float* dummy_array;                                                   //
//                                                                            //
//     (your code to initialize the matrix A)                                 //
//     dummy_array = (float*) malloc(N * sizeof(float));                    //
//     if (dummy_array == NULL) {printf(" No memory available\n"); exit(0); } //
//                                                                            //
//     err = Singular_Value_Decomposition((float*) A, M, N, (float*) U,     //
//                              singular_values, (float*) V, dummy_array);   //
//                                                                            //
//     free(dummy_array);                                                     //
//     if (err < 0) printf(" Failed to converge\n");                          //
//     else { printf(" The singular value decomposition of A is \n");         //
//           ...                                                              //
////////////////////////////////////////////////////////////////////////////////
//                                                                            //
int Singular_Value_Decomposition(float* A, int nrows, int ncols, float* U,
	float* singular_values, float* V, float* dummy_array)
{
	Householders_Reduction_to_Bidiagonal_Form(A, nrows, ncols, U, V,
		singular_values, dummy_array);

	if (Givens_Reduction_to_Diagonal_Form(nrows, ncols, U, V,
		singular_values, dummy_array) < 0) return -1;

	Sort_by_Decreasing_Singular_Values(nrows, ncols, singular_values, U, V);

	return 0;
}


////////////////////////////////////////////////////////////////////////////////
// static void Householders_Reduction_to_Bidiagonal_Form(float* A, int nrows,//
//  int ncols, float* U, float* V, float* diagonal, float* superdiagonal )//
//                                                                            //
//  Description:                                                              //
//     This routine decomposes an m x n matrix A, with m >= n, into a product //
//     of the three matrices U, B, and V', i.e. A = UBV', where U is an m x n //
//     matrix whose columns are orthogonal, B is a n x n bidiagonal matrix,   //
//     and V is an n x n orthogonal matrix.  V' denotes the transpose of V.   //
//     If m < n, then the procedure may be used for the matrix A'.  The       //
//                                                                            //
//     The matrix U is the product of Householder transformations which       //
//     annihilate the subdiagonal components of A while the matrix V is       //
//     the product of Householder transformations which annihilate the        //
//     components of A to the right of the superdiagonal.                     //
//                                                                            //
//     The Householder transformation which leaves invariant the first k-1    //
//     elements of the k-th column and annihilates the all the elements below //
//     the diagonal element is P = I - (2/u'u)uu', u is an nrows-dimensional  //
//     vector the first k-1 components of which are zero and the last         //
//     components agree with the current transformed matrix below the diagonal//
//     diagonal, the remaining k-th element is the diagonal element - s, where//
//     s = (+/-)sqrt(sum of squares of the elements below the diagonal), the  //
//     sign is chosen opposite that of the diagonal element.                  //
//                                                                            //
//  Arguments:                                                                //
//     float* A                                                              //
//        On input, the pointer to the first element of the matrix            //
//        A[nrows][ncols].  The matrix A is unchanged.                        //
//     int nrows                                                              //
//        The number of rows of the matrix A.                                 //
//     int ncols                                                              //
//        The number of columns of the matrix A.                              //
//     float* U                                                              //
//        On input, a pointer to a matrix with the same number of rows and    //
//        columns as the matrix A.  On output, the matrix with mutually       //
//        orthogonal columns which is the left-most factor in the bidiagonal  //
//        decomposition of A.                                                 //
//     float* V                                                              //
//        On input, a pointer to a square matrix with the same number of rows //
//        and columns as the columns of the matrix A, i.e. V[ncols][ncols].   //
//        On output, the orthogonal matrix whose transpose is the right-most  //
//        factor in the bidiagonal decomposition of A.                        //
//     float* diagonal                                                       //
//        On input, a pointer to an array dimensioned to same as the number   //
//        of columns of the matrix A, ncols.  On output, the diagonal of the  //
//        bidiagonal matrix.                                                  //
//     float* superdiagonal                                                  //
//        On input, a pointer to an array dimensioned to same as the number   //
//        of columns of the matrix A, ncols.  On output, the superdiagonal    //
//        of the bidiagonal matrix.                                           //
//                                                                            //
//  Return Values:                                                            //
//     The function is of type void and therefore does not return a value.    //
//     The matrices U, V, and the diagonal and superdiagonal are calculated   //
//     using the addresses passed in the argument list.                       //
//                                                                            //
//  Example:                                                                  //
//     #define M                                                              //
//     #define N                                                              //
//     float A[M][N];                                                        //
//     float U[M][N];                                                        //
//     float V[N][N];                                                        //
//     float diagonal[N];                                                    //
//     float superdiagonal[N];                                               //
//                                                                            //
//     (your code to initialize the matrix A - Note this routine is not       //
//     (accessible from outside i.e. it is declared static)                   //
//                                                                            //
//     Householders_Reduction_to_Bidiagonal_Form((float*) A, nrows, ncols,   //
//                   (float*) U, (float*) V, diagonal, superdiagonal )      //
//                                                                            //
//     free(dummy_array);                                                     //
//           ...                                                              //
////////////////////////////////////////////////////////////////////////////////
//                                                                            //
static void Householders_Reduction_to_Bidiagonal_Form(float* A, int nrows,
	int ncols, float* U, float* V, float* diagonal, float* superdiagonal)
{
	int i, j, k, ip1;
	float s, s2, si, scale;
	float dum;
	float *pu, *pui, *pv, *pvi;
	float half_norm_squared;

	// Copy A to U

	memcpy(U, A, sizeof(float) * nrows * ncols);

	//

	diagonal[0] = 0.0;
	s = 0.0;
	scale = 0.0;
	for (i = 0, pui = U, ip1 = 1; i < ncols; pui += ncols, i++, ip1++) {
		superdiagonal[i] = scale * s;
		//       
		//                  Perform Householder transform on columns.
		//
		//       Calculate the normed squared of the i-th column vector starting at 
		//       row i.
		//
		for (j = i, pu = pui, scale = 0.0; j < nrows; j++, pu += ncols)
		{
			if (*(pu + i) < 0)
			{
				scale += -(*(pu + i));
			}
			else
			{
				scale += (*(pu + i));
			}
		}

		if (scale > 0.0) {
			for (j = i, pu = pui, s2 = 0.0; j < nrows; j++, pu += ncols) {
				*(pu + i) /= scale;
				s2 += *(pu + i) * *(pu + i);
			}
			//
			//    
			//       Chose sign of s which maximizes the norm
			//  
			s = (*(pui + i) < 0.0) ? sqrt(s2) : -sqrt(s2);
			//
			//       Calculate -2/u'u
			//
			half_norm_squared = *(pui + i) * s - s2;
			//
			//       Transform remaining columns by the Householder transform.
			//
			*(pui + i) -= s;

			for (j = ip1; j < ncols; j++) {
				for (k = i, si = 0.0, pu = pui; k < nrows; k++, pu += ncols)
					si += *(pu + i) * *(pu + j);
				si /= half_norm_squared;
				for (k = i, pu = pui; k < nrows; k++, pu += ncols) {
					*(pu + j) += si * *(pu + i);
				}
			}
		}
		for (j = i, pu = pui; j < nrows; j++, pu += ncols) *(pu + i) *= scale;
		diagonal[i] = s * scale;
		//       
		//                  Perform Householder transform on rows.
		//
		//       Calculate the normed squared of the i-th row vector starting at 
		//       column i.
		//
		s = 0.0;
		scale = 0.0;
		if (i >= nrows || i == (ncols - 1)) continue;
		for (j = ip1; j < ncols; j++) scale += fabs(*(pui + j));
		if (scale > 0.0) {
			for (j = ip1, s2 = 0.0; j < ncols; j++) {
				*(pui + j) /= scale;
				s2 += *(pui + j) * *(pui + j);
			}
			s = (*(pui + ip1) < 0.0) ? sqrt(s2) : -sqrt(s2);
			//
			//       Calculate -2/u'u
			//
			half_norm_squared = *(pui + ip1) * s - s2;
			//
			//       Transform the rows by the Householder transform.
			//
			*(pui + ip1) -= s;
			for (k = ip1; k < ncols; k++)
				superdiagonal[k] = *(pui + k) / half_norm_squared;
			if (i < (nrows - 1)) {
				for (j = ip1, pu = pui + ncols; j < nrows; j++, pu += ncols) {
					for (k = ip1, si = 0.0; k < ncols; k++)
						si += *(pui + k) * *(pu + k);
					for (k = ip1; k < ncols; k++) {
						*(pu + k) += si * superdiagonal[k];
					}
				}
			}
			for (k = ip1; k < ncols; k++) *(pui + k) *= scale;
		}
	}

	// Update V
	pui = U + ncols * (ncols - 2);
	pvi = V + ncols * (ncols - 1);
	*(pvi + ncols - 1) = 1.0;
	s = superdiagonal[ncols - 1];
	pvi -= ncols;
	for (i = ncols - 2, ip1 = ncols - 1; i >= 0; i--, pui -= ncols,
		pvi -= ncols, ip1--) {
		if (s != 0.0) {
			pv = pvi + ncols;
			for (j = ip1; j < ncols; j++, pv += ncols)
				*(pv + i) = (*(pui + j) / *(pui + ip1)) / s;
			for (j = ip1; j < ncols; j++) {
				si = 0.0;
				for (k = ip1, pv = pvi + ncols; k < ncols; k++, pv += ncols)
					si += *(pui + k) * *(pv + j);
				for (k = ip1, pv = pvi + ncols; k < ncols; k++, pv += ncols)
					*(pv + j) += si * *(pv + i);
			}
		}
		pv = pvi + ncols;
		for (j = ip1; j < ncols; j++, pv += ncols) {
			*(pvi + j) = 0.0;
			*(pv + i) = 0.0;
		}
		*(pvi + i) = 1.0;
		s = superdiagonal[i];
	}

	// Update U

	pui = U + ncols * (ncols - 1);
	for (i = ncols - 1, ip1 = ncols; i >= 0; ip1 = i, i--, pui -= ncols) {
		s = diagonal[i];
		for (j = ip1; j < ncols; j++) *(pui + j) = 0.0;
		if (s != 0.0) {
			for (j = ip1; j < ncols; j++) {
				si = 0.0;
				pu = pui + ncols;
				for (k = ip1; k < nrows; k++, pu += ncols)
					si += *(pu + i) * *(pu + j);
				si = (si / *(pui + i)) / s;
				for (k = i, pu = pui; k < nrows; k++, pu += ncols)
					*(pu + j) += si * *(pu + i);
			}
			for (j = i, pu = pui; j < nrows; j++, pu += ncols) {
				*(pu + i) /= s;
			}
		}
		else
			for (j = i, pu = pui; j < nrows; j++, pu += ncols) *(pu + i) = 0.0;
		*(pui + i) += 1.0;
	}
}


////////////////////////////////////////////////////////////////////////////////
// static int Givens_Reduction_to_Diagonal_Form( int nrows, int ncols,        //
//         float* U, float* V, float* diagonal, float* superdiagonal )    //
//                                                                            //
//  Description:                                                              //
//     This routine decomposes a bidiagonal matrix given by the arrays        //
//     diagonal and superdiagonal into a product of three matrices U1, D and  //
//     V1', the matrix U1 premultiplies U and is returned in U, the matrix    //
//     V1 premultiplies V and is returned in V.  The matrix D is a diagonal   //
//     matrix and replaces the array diagonal.                                //
//                                                                            //
//     The method used to annihilate the offdiagonal elements is a variant    //
//     of the QR transformation.  The method consists of applying Givens      //
//     rotations to the right and the left of the current matrix until        //
//     the new off-diagonal elements are chased out of the matrix.            //
//                                                                            //
//     The process is an iterative process which due to roundoff errors may   //
//     not converge within a predefined number of iterations.  (This should   //
//     be unusual.)                                                           //
//                                                                            //
//  Arguments:                                                                //
//     int nrows                                                              //
//        The number of rows of the matrix U.                                 //
//     int ncols                                                              //
//        The number of columns of the matrix U.                              //
//     float* U                                                              //
//        On input, a pointer to a matrix already initialized to a matrix     //
//        with mutually orthogonal columns.   On output, the matrix with      //
//        mutually orthogonal columns.                                        //
//     float* V                                                              //
//        On input, a pointer to a square matrix with the same number of rows //
//        and columns as the columns of the matrix U, i.e. V[ncols][ncols].   //
//        The matrix V is assumed to be initialized to an orthogonal matrix.  //
//        On output, V is an orthogonal matrix.                               //
//     float* diagonal                                                       //
//        On input, a pointer to an array of dimension ncols which initially  //
//        contains the diagonal of the bidiagonal matrix.  On output, the     //
//        it contains the diagonal of the diagonal matrix.                    //
//     float* superdiagonal                                                  //
//        On input, a pointer to an array of dimension ncols which initially  //
//        the first component is zero and the successive components form the  //
//        superdiagonal of the bidiagonal matrix.                             //
//                                                                            //
//  Return Values:                                                            //
//     0  Success                                                             //
//    -1  Failure - The procedure failed to terminate within                  //
//                  MAX_ITERATION_COUNT iterations.                           //
//                                                                            //
//  Example:                                                                  //
//     #define M                                                              //
//     #define N                                                              //
//     float U[M][N];                                                        //
//     float V[N][N];                                                        //
//     float diagonal[N];                                                    //
//     float superdiagonal[N];                                               //
//     int err;                                                               //
//                                                                            //
//     (your code to initialize the matrices U, V, diagonal, and )            //
//     ( superdiagonal.  - Note this routine is not accessible from outside)  //
//     ( i.e. it is declared static.)                                         //
//                                                                            //
//     err = Givens_Reduction_to_Diagonal_Form( M,N,(float*)U,(float*)V,    //
//                                                 diagonal, superdiagonal ); //
//     if ( err < 0 ) printf("Failed to converge\n");                         //
//     else { ... }                                                           //
//           ...                                                              //
////////////////////////////////////////////////////////////////////////////////
//                                                                            //
static int Givens_Reduction_to_Diagonal_Form(int nrows, int ncols,
	float* U, float* V, float* diagonal, float* superdiagonal)
{

	float epsilon;
	float c, s;
	float f, g, h;
	float x, y, z;
	float *pu, *pv;
	int i, j, k, m;
	int rotation_test;
	int iteration_count;

	for (i = 0, x = 0.0; i < ncols; i++) {
		y = fabs(diagonal[i]) + fabs(superdiagonal[i]);
		if (x < y) x = y;
	}
	epsilon = x * FLT_EPSILON;
	for (k = ncols - 1; k >= 0; k--) {
		iteration_count = 0;
		while (1) {
			rotation_test = 1;
			for (m = k; m >= 0; m--) {
				if (fabs(superdiagonal[m]) <= epsilon) { rotation_test = 0; break; }
				if (fabs(diagonal[m - 1]) <= epsilon) break;
			}
			if (rotation_test) {
				c = 0.0;
				s = 1.0;
				for (i = m; i <= k; i++) {
					f = s * superdiagonal[i];
					superdiagonal[i] *= c;
					if (fabs(f) <= epsilon) break;
					g = diagonal[i];
					h = sqrt(f*f + g*g);
					diagonal[i] = h;
					c = g / h;
					s = -f / h;
					for (j = 0, pu = U; j < nrows; j++, pu += ncols) {
						y = *(pu + m - 1);
						z = *(pu + i);
						*(pu + m - 1) = y * c + z * s;
						*(pu + i) = -y * s + z * c;
					}
				}
			}
			z = diagonal[k];
			if (m == k) {
				if (z < 0.0) {
					diagonal[k] = -z;
					for (j = 0, pv = V; j < ncols; j++, pv += ncols)
						*(pv + k) = -*(pv + k);
				}
				break;
			}
			else {
				if (iteration_count >= MAX_ITERATION_COUNT) return -1;
				iteration_count++;
				x = diagonal[m];
				y = diagonal[k - 1];
				g = superdiagonal[k - 1];
				h = superdiagonal[k];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0 * h * y);
				g = sqrt(f * f + 1.0);
				if (f < 0.0) g = -g;
				f = ((x - z) * (x + z) + h * (y / (f + g) - h)) / x;
				// Next QR Transformtion
				c = 1.0;
				s = 1.0;
				for (i = m + 1; i <= k; i++) {
					g = superdiagonal[i];
					y = diagonal[i];
					h = s * g;
					g *= c;
					z = sqrt(f * f + h * h);
					superdiagonal[i - 1] = z;
					c = f / z;
					s = h / z;
					f = x * c + g * s;
					g = -x * s + g * c;
					h = y * s;
					y *= c;
					for (j = 0, pv = V; j < ncols; j++, pv += ncols) {
						x = *(pv + i - 1);
						z = *(pv + i);
						*(pv + i - 1) = x * c + z * s;
						*(pv + i) = -x * s + z * c;
					}
					z = sqrt(f * f + h * h);
					diagonal[i - 1] = z;
					if (z != 0.0) {
						c = f / z;
						s = h / z;
					}
					f = c * g + s * y;
					x = -s * g + c * y;
					for (j = 0, pu = U; j < nrows; j++, pu += ncols) {
						y = *(pu + i - 1);
						z = *(pu + i);
						*(pu + i - 1) = c * y + s * z;
						*(pu + i) = -s * y + c * z;
					}
				}
				superdiagonal[m] = 0.0;
				superdiagonal[k] = f;
				diagonal[k] = x;
			}
		}
	}
	return 0;
}


////////////////////////////////////////////////////////////////////////////////
// static void Sort_by_Decreasing_Singular_Values(int nrows, int ncols,       //
//                            float* singular_values, float* U, float* V)  //
//                                                                            //
//  Description:                                                              //
//     This routine sorts the singular values from largest to smallest        //
//     singular value and interchanges the columns of U and the columns of V  //
//     whenever a lead_swap is made.  I.e. if the i-th singular value is swapped   //
//     with the j-th singular value, then the i-th and j-th columns of U are  //
//     interchanged and the i-th and j-th columns of V are interchanged.      //
//                                                                            //
//  Arguments:                                                                //
//     int nrows                                                              //
//        The number of rows of the matrix U.                                 //
//     int ncols                                                              //
//        The number of columns of the matrix U.                              //
//     float* singular_values                                                //
//        On input, a pointer to the array of singular values.  On output, the//
//        sorted array of singular values.                                    //
//     float* U                                                              //
//        On input, a pointer to a matrix already initialized to a matrix     //
//        with mutually orthogonal columns.  On output, the matrix with       //
//        mutually orthogonal possibly permuted columns.                      //
//     float* V                                                              //
//        On input, a pointer to a square matrix with the same number of rows //
//        and columns as the columns of the matrix U, i.e. V[ncols][ncols].   //
//        The matrix V is assumed to be initialized to an orthogonal matrix.  //
//        On output, V is an orthogonal matrix with possibly permuted columns.//
//                                                                            //
//  Return Values:                                                            //
//        The function is of type void.                                       //
//                                                                            //
//  Example:                                                                  //
//     #define M                                                              //
//     #define N                                                              //
//     float U[M][N];                                                        //
//     float V[N][N];                                                        //
//     float diagonal[N];                                                    //
//                                                                            //
//     (your code to initialize the matrices U, V, and diagonal. )            //
//     ( - Note this routine is not accessible from outside)                  //
//     ( i.e. it is declared static.)                                         //
//                                                                            //
//     Sort_by_Decreasing_Singular_Values(nrows, ncols, singular_values,      //
//                                                 (float*) U, (float*) V); //
//           ...                                                              //
////////////////////////////////////////////////////////////////////////////////
//                                                                            //
static void Sort_by_Decreasing_Singular_Values(int nrows, int ncols,
	float* singular_values, float* U, float* V)
{
	int i, j, max_index;
	float temp;
	float *p1, *p2;

	for (i = 0; i < ncols - 1; i++) {
		max_index = i;
		for (j = i + 1; j < ncols; j++)
			if (singular_values[j] > singular_values[max_index])
				max_index = j;
		if (max_index == i) continue;
		temp = singular_values[i];
		singular_values[i] = singular_values[max_index];
		singular_values[max_index] = temp;
		p1 = U + max_index;
		p2 = U + i;
		for (j = 0; j < nrows; j++, p1 += ncols, p2 += ncols) {
			temp = *p1;
			*p1 = *p2;
			*p2 = temp;
		}
		p1 = V + max_index;
		p2 = V + i;
		for (j = 0; j < ncols; j++, p1 += ncols, p2 += ncols) {
			temp = *p1;
			*p1 = *p2;
			*p2 = temp;
		}
	}
}


////////////////////////////////////////////////////////////////////////////////
//  void Singular_Value_Decomposition_Solve(float* U, float* D, float* V,  //
//              float tolerance, int nrows, int ncols, float *B, float* x) //
//                                                                            //
//  Description:                                                              //
//     This routine solves the system of linear equations Ax=B where A =UDV', //
//     is the singular value decomposition of A.  Given UDV'x=B, then         //
//     x = V(1/D)U'B, where 1/D is the pseudo-inverse of D, i.e. if D[i] > 0  //
//     then (1/D)[i] = 1/D[i] and if D[i] = 0, then (1/D)[i] = 0.  Since      //
//     the singular values are subject to round-off error.  A tolerance is    //
//     given so that if D[i] < tolerance, D[i] is treated as if it is 0.      //
//     The default tolerance is D[0] * FLT_EPSILON * ncols, if the user       //
//     specified tolerance is less than the default tolerance, the default    //
//     tolerance is used.                                                     //
//                                                                            //
//  Arguments:                                                                //
//     float* U                                                              //
//        A matrix with mutually orthonormal columns.                         //
//     float* D                                                              //
//        A diagonal matrix with decreasing non-negative diagonal elements.   //
//        i.e. D[i] > D[j] if i < j and D[i] >= 0 for all i.                  //
//     float* V                                                              //
//        An orthogonal matrix.                                               //
//     float tolerance                                                       //
//        An lower bound for non-zero singular values (provided tolerance >   //
//        ncols * FLT_EPSILON * D[0]).                                        //
//     int nrows                                                              //
//        The number of rows of the matrix U and B.                           //
//     int ncols                                                              //
//        The number of columns of the matrix U.  Also the number of rows and //
//        columns of the matrices D and V.                                    //
//     float* B                                                              //
//        A pointer to a vector dimensioned as nrows which is the  right-hand //
//        side of the equation Ax = B where A = UDV'.                         //
//     float* x                                                              //
//        A pointer to a vector dimensioned as ncols, which is the least      //
//        squares solution of the equation Ax = B where A = UDV'.             //
//                                                                            //
//  Return Values:                                                            //
//        The function is of type void.                                       //
//                                                                            //
//  Example:                                                                  //
//     #define M                                                              //
//     #define N                                                              //
//     #define NB                                                             //
//     float U[M][N];                                                        //
//     float V[N][N];                                                        //
//     float D[N];                                                           //
//     float B[M];                                                           //
//     float x[N];                                                           //
//     float tolerance;                                                      //
//                                                                            //
//     (your code to initialize the matrices U,D,V,B)                         //
//                                                                            //
//     Singular_Value_Decomposition_Solve((float*) U, D, (float*) V,        //
//                                              tolerance, M, N, B, x, bcols) //
//                                                                            //
//     printf(" The solution of Ax=B is \n");                                 //
//           ...                                                              //
////////////////////////////////////////////////////////////////////////////////
//                                                                            //

void Singular_Value_Decomposition_Solve(float* U, float* D, float* V,
	float tolerance, int nrows, int ncols, float *B, float* x)
{
	int i, j, k;
	float *pu, *pv;
	float dum;

	dum = FLT_EPSILON * D[0] * (float)ncols;
	if (tolerance < dum) tolerance = dum;

	for (i = 0, pv = V; i < ncols; i++, pv += ncols) {
		x[i] = 0.0;
		for (j = 0; j < ncols; j++)
			if (D[j] > tolerance) {
				for (k = 0, dum = 0.0, pu = U; k < nrows; k++, pu += ncols)
					dum += *(pu + j) * B[k];
				x[i] += dum * *(pv + j) / D[j];
			}
	}
}


////////////////////////////////////////////////////////////////////////////////
//  void Singular_Value_Decomposition_Inverse(float* U, float* D, float* V,//
//                     float tolerance, int nrows, int ncols, float *Astar) //
//                                                                            //
//  Description:                                                              //
//     This routine calculates the pseudo-inverse of the matrix A = UDV'.     //
//     where U, D, V constitute the singular value decomposition of A.        //
//     Let Astar be the pseudo-inverse then Astar = V(1/D)U', where 1/D is    //
//     the pseudo-inverse of D, i.e. if D[i] > 0 then (1/D)[i] = 1/D[i] and   //
//     if D[i] = 0, then (1/D)[i] = 0.  Because the singular values are       //
//     subject to round-off error.  A tolerance is given so that if           //
//     D[i] < tolerance, D[i] is treated as if it were 0.                     //
//     The default tolerance is D[0] * FLT_EPSILON * ncols, assuming that the //
//     diagonal matrix of singular values is sorted from largest to smallest, //
//     if the user specified tolerance is less than the default tolerance,    //
//     then the default tolerance is used.                                    //
//                                                                            //
//  Arguments:                                                                //
//     float* U                                                              //
//        A matrix with mutually orthonormal columns.                         //
//     float* D                                                              //
//        A diagonal matrix with decreasing non-negative diagonal elements.   //
//        i.e. D[i] > D[j] if i < j and D[i] >= 0 for all i.                  //
//     float* V                                                              //
//        An orthogonal matrix.                                               //
//     float tolerance                                                       //
//        An lower bound for non-zero singular values (provided tolerance >   //
//        ncols * FLT_EPSILON * D[0]).                                        //
//     int nrows                                                              //
//        The number of rows of the matrix U and B.                           //
//     int ncols                                                              //
//        The number of columns of the matrix U.  Also the number of rows and //
//        columns of the matrices D and V.                                    //
//     float* Astar                                                          //
//        On input, a pointer to the first element of an ncols x nrows matrix.//
//        On output, the pseudo-inverse of UDV'.                              //
//                                                                            //
//  Return Values:                                                            //
//        The function is of type void.                                       //
//                                                                            //
//  Example:                                                                  //
//     #define M                                                              //
//     #define N                                                              //
//     float U[M][N];                                                        //
//     float V[N][N];                                                        //
//     float D[N];                                                           //
//     float Astar[N][M];                                                    //
//     float tolerance;                                                      //
//                                                                            //
//     (your code to initialize the matrices U,D,V)                           //
//                                                                            //
//     Singular_Value_Decomposition_Inverse((float*) U, D, (float*) V,      //
//                                        tolerance, M, N, (float*) Astar);  //
//                                                                            //
//     printf(" The pseudo-inverse of A = UDV' is \n");                       //
//           ...                                                              //
////////////////////////////////////////////////////////////////////////////////
//                                                                            //

void Singular_Value_Decomposition_Inverse(float* U, float* D, float* V,
	float tolerance, int nrows, int ncols, float *Astar)
{
	int i, j, k;
	float *pu, *pv, *pa;
	float dum;

	dum = FLT_EPSILON * D[0] * (float)ncols;
	if (tolerance < dum) tolerance = dum;
	for (i = 0, pv = V, pa = Astar; i < ncols; i++, pv += ncols)
		for (j = 0, pu = U; j < nrows; j++, pa++)
			for (k = 0, *pa = 0.0; k < ncols; k++, pu++)
				if (D[k] > tolerance) *pa += *(pv + k) * *pu / D[k];
}
#if 0
#define SIGN(a, b) ((b) >= 0.0 ? fabs(a) : -fabs(a))
#define MAX(x,y) ((x)>(y)?(x):(y))


static float PYTHAG(float a, float b)
{
	//float at = fabs(a);
	//float bt = fabs(b);
	float at;
	float bt;
	float ct, result;

	if (a >= 0)
		at = a;
	else
		at = -a;

	if (b >= 0)
		bt = b;
	else
		bt = -b;

	if (at > bt) { ct = bt / at; result = at * sqrt(1.0 + ct * ct); }
	else if (bt > 0.0) { ct = at / bt; result = bt * sqrt(1.0 + ct * ct); }
	else result = 0.0;
	return(result);
}


int dsvd(float a[][MA_SIZE_L], int m, int n, float *w, float v[][MA_SIZE_L])
{
	int flag, i, its, j, jj, k, l, nm;
	float c, f, h, s, x, y, z;
	float anorm = 0.0, g = 0.0, scale = 0.0;

	float temp1, temp2;

	static float rv1[WIN_SIZE_DECI];
	if (m < n)
	{
		//fprintf(stderr, "#rows must be > #cols \n");
		return(0);
	}



	/* Householder reduction to bidiagonal form */
	for (i = 0; i < n; i++)
	{
		/* left-hand reduction */
		l = i + 1;
		rv1[i] = scale * g;
		g = s = scale = 0.0;
		if (i < m)
		{
			for (k = i; k < m; k++)
			{
				if (a[k][i] >= 0)
					scale += (float)a[k][i];
				else
					scale -= (float)a[k][i];
				//scale += fabs((float)a[k][i]);
			}
			if (scale)
			{
				for (k = i; k < m; k++)
				{
					a[k][i] = (float)((float)a[k][i] / scale);
					s += ((float)a[k][i] * (float)a[k][i]);
				}
				f = (float)a[i][i];
				if (f >= 0.0)
				{
					g = -sqrt(s);
				}
				else
				{
					g = sqrt(s);
				}
				//g = -SIGN(sqrt(s), f);
				h = f * g - s;
				a[i][i] = (float)(f - g);
				if (i != n - 1)
				{
					for (j = l; j < n; j++)
					{
						for (s = 0.0, k = i; k < m; k++)
							s += ((float)a[k][i] * (float)a[k][j]);
						f = s / h;
						for (k = i; k < m; k++)
							a[k][j] += (float)(f * (float)a[k][i]);
					}
				}
				for (k = i; k < m; k++)
					a[k][i] = (float)((float)a[k][i] * scale);
			}
		}
		w[i] = (float)(scale * g);

		/* right-hand reduction */
		g = s = scale = 0.0;
		if (i < m && i != n - 1)
		{
			for (k = l; k < n; k++)
			{
				if (a[i][k] >= 0)
					scale += (float)a[i][k];
				else
					scale -= (float)a[i][k];
				//scale += fabs((float)a[i][k]);
			}
			if (scale)
			{
				for (k = l; k < n; k++)
				{
					a[i][k] = (float)((float)a[i][k] / scale);
					s += ((float)a[i][k] * (float)a[i][k]);
				}
				f = (float)a[i][l];
				//g = -SIGN(sqrt(s), f);
				if (f >= 0.0)
				{
					g = -sqrt(s);
				}
				else
				{
					g = sqrt(s);
				}
				h = f * g - s;
				a[i][l] = (float)(f - g);
				for (k = l; k < n; k++)
					rv1[k] = (float)a[i][k] / h;
				if (i != m - 1)
				{
					for (j = l; j < m; j++)
					{
						for (s = 0.0, k = l; k < n; k++)
							s += ((float)a[j][k] * (float)a[i][k]);
						for (k = l; k < n; k++)
							a[j][k] += (float)(s * rv1[k]);
					}
				}
				for (k = l; k < n; k++)
					a[i][k] = (float)((float)a[i][k] * scale);
			}
		}
		//anorm = MAX(anorm, (fabs((float)w[i]) + fabs(rv1[i])));

		if (w[i] >= 0)
			temp1 = (float)w[i];
		else
			temp1 = -(float)w[i];

		if (rv1[i] >= 0)
			temp2 = rv1[i];
		else
			temp2 = -rv1[i];
		//anorm = MAX(anorm, (temp1 + temp2));
		if (anorm > (temp1 + temp2))
			anorm = anorm;
		else
			anorm = temp1 + temp2;
	}

	/* accumulate the right-hand transformation */
	for (i = n - 1; i >= 0; i--)
	{
		if (i < n - 1)
		{
			if (g)
			{
				for (j = l; j < n; j++)
					v[j][i] = (float)(((float)a[i][j] / (float)a[i][l]) / g);
				/* float division to avoid underflow */
				for (j = l; j < n; j++)
				{
					for (s = 0.0, k = l; k < n; k++)
						s += ((float)a[i][k] * (float)v[k][j]);
					for (k = l; k < n; k++)
						v[k][j] += (float)(s * (float)v[k][i]);
				}
			}
			for (j = l; j < n; j++)
				v[i][j] = v[j][i] = 0.0;
		}
		v[i][i] = 1.0;
		g = rv1[i];
		l = i;
	}

	/* accumulate the left-hand transformation */
	for (i = n - 1; i >= 0; i--)
	{
		l = i + 1;
		g = (float)w[i];
		if (i < n - 1)
			for (j = l; j < n; j++)
				a[i][j] = 0.0;
		if (g)
		{
			g = 1.0 / g;
			if (i != n - 1)
			{
				for (j = l; j < n; j++)
				{
					for (s = 0.0, k = l; k < m; k++)
						s += ((float)a[k][i] * (float)a[k][j]);
					f = (s / (float)a[i][i]) * g;
					for (k = i; k < m; k++)
						a[k][j] += (float)(f * (float)a[k][i]);
				}
			}
			for (j = i; j < m; j++)
				a[j][i] = (float)((float)a[j][i] * g);
		}
		else
		{
			for (j = i; j < m; j++)
				a[j][i] = 0.0;
		}
		++a[i][i];
	}

	/* diagonalize the bidiagonal form */
	for (k = n - 1; k >= 0; k--)
	{                             /* loop over singular values */
		for (its = 0; its < 30; its++)
		{                         /* loop over allowed iterations */
			flag = 1;
			for (l = k; l >= 0; l--)
			{                     /* test for splitting */
				nm = l - 1;
				/*
				if (fabs(rv1[l]) + anorm == anorm)
				{
				flag = 0;
				break;
				}
				*/
				if (rv1[l] >= 0)
					temp1 = rv1[l];
				else
					temp1 = -rv1[l];
				if (temp1 + anorm == anorm)
				{
					flag = 0;
					break;
				}

				//if (fabs((float)w[nm]) + anorm == anorm)
				//	break;



				if (w[nm] >= 0)
					temp2 = (float)w[nm];
				else
					temp2 = -(float)w[nm];
				if (temp2 + anorm == anorm)
					break;
			}
			if (flag)
			{
				c = 0.0;
				s = 1.0;
				for (i = l; i <= k; i++)
				{
					f = s * rv1[i];
					if (f >= 0)
						f = f;
					else
						f = -f;
					//if (fabs(f) + anorm != anorm)
					if (f + anorm != anorm)
					{
						g = (float)w[i];
						h = PYTHAG(f, g);
						w[i] = (float)h;
						h = 1.0 / h;
						c = g * h;
						s = (-f * h);
						for (j = 0; j < m; j++)
						{
							y = (float)a[j][nm];
							z = (float)a[j][i];
							a[j][nm] = (float)(y * c + z * s);
							a[j][i] = (float)(z * c - y * s);
						}
					}
				}
			}
			z = (float)w[k];
			if (l == k)
			{                  /* convergence */
				if (z < 0.0)
				{              /* make singular value nonnegative */
					w[k] = (float)(-z);
					for (j = 0; j < n; j++)
						v[j][k] = (-v[j][k]);
				}
				break;
			}
			if (its >= 30) {
				//	free((void*)rv1);
				//fprintf(stderr, "No convergence after 30,000! iterations \n");
				return(0);
			}

			/* shift from bottom 2 x 2 minor */
			x = (float)w[l];
			nm = k - 1;
			y = (float)w[nm];
			g = rv1[nm];
			h = rv1[k];
			f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0 * h * y);
			g = PYTHAG(f, 1.0);
			if (f >= 0)
			{
				if (g >= 0)
					f = ((x - z) * (x + z) + h * ((y / (f + g)) - h)) / x;
				else
					f = ((x - z) * (x + z) + h * ((y / (f - g)) - h)) / x;
			}
			else
			{
				if (g >= 0)
					f = ((x - z) * (x + z) + h * ((y / (f - g)) - h)) / x;
				else
					f = ((x - z) * (x + z) + h * ((y / (f + g)) - h)) / x;
			}
			/* next QR transformation */
			c = s = 1.0;
			for (j = l; j <= nm; j++)
			{
				i = j + 1;
				g = rv1[i];
				y = (float)w[i];
				h = s * g;
				g = c * g;
				z = PYTHAG(f, h);
				rv1[j] = z;
				c = f / z;
				s = h / z;
				f = x * c + g * s;
				g = g * c - x * s;
				h = y * s;
				y = y * c;
				for (jj = 0; jj < n; jj++)
				{
					x = (float)v[jj][j];
					z = (float)v[jj][i];
					v[jj][j] = (float)(x * c + z * s);
					v[jj][i] = (float)(z * c - x * s);
				}
				z = PYTHAG(f, h);
				w[j] = (float)z;
				if (z)
				{
					z = 1.0 / z;
					c = f * z;
					s = h * z;
				}
				f = (c * g) + (s * y);
				x = (c * y) - (s * g);
				for (jj = 0; jj < m; jj++)
				{
					y = (float)a[jj][j];
					z = (float)a[jj][i];
					a[jj][j] = (float)(y * c + z * s);
					a[jj][i] = (float)(z * c - y * s);
				}
			}
			rv1[l] = 0.0;
			rv1[k] = f;
			w[k] = (float)x;
		}
	}
	//free((void*)rv1);
	return(1);
}
#endif