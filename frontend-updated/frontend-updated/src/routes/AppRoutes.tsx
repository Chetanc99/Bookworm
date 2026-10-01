import React, { lazy, Suspense } from "react";
import { Routes, Route } from "react-router-dom";
import { MainLayout } from "../components/layout/MainLayout";
import { AdminLayout } from "../components/layout/AdminLayout";
import { ProtectedRoute } from "./ProtectedRoute";
import { Skeleton } from "../components/ui/Skeleton";

// LAZY LOADED CUSTOMER & AUTH PAGES
const HomePage = lazy(() =>
  import("../pages/customer/HomePage").then((m) => ({ default: m.HomePage }))
);
const ProductsPage = lazy(() =>
  import("../pages/customer/ProductsPage").then((m) => ({ default: m.ProductsPage }))
);
const LibraryPackagesPage = lazy(() =>
  import("../pages/customer/LibraryPackagesPage").then((m) => ({ default: m.LibraryPackagesPage }))
);
const SubscribeConfirmPage = lazy(() =>
  import("../pages/customer/SubscribeConfirmPage").then((m) => ({ default: m.SubscribeConfirmPage }))
);
const CartPage = lazy(() =>
  import("../pages/customer/CartPage").then((m) => ({ default: m.CartPage }))
);
const TransactionPage = lazy(() =>
  import("../pages/customer/TransactionPage").then((m) => ({ default: m.TransactionPage }))
);
const PaymentPage = lazy(() =>
  import("../pages/customer/PaymentPage").then((m) => ({ default: m.PaymentPage }))
);
const SuccessPage = lazy(() =>
  import("../pages/customer/SuccessPage").then((m) => ({ default: m.SuccessPage }))
);
const ShelfPage = lazy(() =>
  import("../pages/customer/ShelfPage").then((m) => ({ default: m.ShelfPage }))
);
const OrdersPage = lazy(() =>
  import("../pages/customer/OrdersPage").then((m) => ({ default: m.OrdersPage }))
);
const MySubscriptionPage = lazy(() =>
  import("../pages/customer/MySubscriptionPage").then((m) => ({ default: m.MySubscriptionPage }))
);

const LoginPage = lazy(() =>
  import("../pages/auth/LoginPage").then((m) => ({ default: m.LoginPage }))
);
const RegisterPage = lazy(() =>
  import("../pages/auth/RegisterPage").then((m) => ({ default: m.RegisterPage }))
);
const ForgotPasswordPage = lazy(() =>
  import("../pages/auth/ForgotPasswordPage").then((m) => ({ default: m.ForgotPasswordPage }))
);
const ResetPasswordPage = lazy(() =>
  import("../pages/auth/ResetPasswordPage").then((m) => ({ default: m.ResetPasswordPage }))
);
const VerifyEmailPage = lazy(() =>
  import("../pages/auth/VerifyEmailPage").then((m) => ({ default: m.VerifyEmailPage }))
);
const OAuthCallbackPage = lazy(() =>
  import("../pages/auth/OAuthCallbackPage").then((m) => ({ default: m.OAuthCallbackPage }))
);
const NotFoundPage = lazy(() =>
  import("../pages/NotFoundPage").then((m) => ({ default: m.NotFoundPage }))
);

// LAZY LOADED ADMIN PAGES
const AdminDashboardPage = lazy(() =>
  import("../pages/admin/AdminDashboardPage").then((m) => ({ default: m.AdminDashboardPage }))
);
const ManageProductsPage = lazy(() =>
  import("../pages/admin/ManageProductsPage").then((m) => ({ default: m.ManageProductsPage }))
);
const AddProductPage = lazy(() =>
  import("../pages/admin/AddProductPage").then((m) => ({ default: m.AddProductPage }))
);
const UsersPage = lazy(() =>
  import("../pages/admin/UsersPage").then((m) => ({ default: m.UsersPage }))
);
const TransactionsPage = lazy(() =>
  import("../pages/admin/TransactionsPage").then((m) => ({ default: m.TransactionsPage }))
);
const RoyaltiesPage = lazy(() =>
  import("../pages/admin/RoyaltiesPage").then((m) => ({ default: m.RoyaltiesPage }))
);
const BeneficiariesPage = lazy(() =>
  import("../pages/admin/BeneficiariesPage").then((m) => ({ default: m.BeneficiariesPage }))
);
const AuditLogsPage = lazy(() =>
  import("../pages/admin/AuditLogsPage").then((m) => ({ default: m.AuditLogsPage }))
);

const PageFallback = () => (
  <div className="max-w-7xl mx-auto p-8 space-y-6">
    <Skeleton className="h-12 w-1/3 rounded-2xl" />
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      <Skeleton className="h-64 rounded-3xl" />
      <Skeleton className="h-64 rounded-3xl" />
      <Skeleton className="h-64 rounded-3xl" />
    </div>
  </div>
);

export const AppRoutes: React.FC = () => {
  return (
    <Suspense fallback={<PageFallback />}>
      <Routes>
        {/* PUBLIC STORE & CUSTOMER ROUTES */}
        <Route
          path="/"
          element={
            <MainLayout>
              <HomePage />
            </MainLayout>
          }
        />
        <Route
          path="/products"
          element={
            <MainLayout>
              <ProductsPage />
            </MainLayout>
          }
        />
        <Route
          path="/library"
          element={
            <MainLayout>
              <LibraryPackagesPage />
            </MainLayout>
          }
        />
        <Route
          path="/library/subscribe/:libraryPackageId"
          element={
            <MainLayout>
              <SubscribeConfirmPage />
            </MainLayout>
          }
        />

        {/* PROTECTED CUSTOMER ROUTES */}
        <Route
          path="/cart"
          element={
            <ProtectedRoute>
              <MainLayout>
                <CartPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/transaction"
          element={
            <ProtectedRoute>
              <MainLayout>
                <TransactionPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/payment"
          element={
            <ProtectedRoute>
              <MainLayout>
                <PaymentPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/success"
          element={
            <ProtectedRoute>
              <MainLayout>
                <SuccessPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/shelf"
          element={
            <ProtectedRoute>
              <MainLayout>
                <ShelfPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute>
              <MainLayout>
                <OrdersPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-library"
          element={
            <ProtectedRoute>
              <MainLayout>
                <MySubscriptionPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        {/* AUTHENTICATION ROUTES */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/oauth2/callback" element={<OAuthCallbackPage />} />

        {/* PROTECTED ADMIN ROUTES */}
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute adminOnly>
              <AdminLayout>
                <AdminDashboardPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/products"
          element={
            <ProtectedRoute adminOnly>
              <AdminLayout>
                <ManageProductsPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/add-product"
          element={
            <ProtectedRoute adminOnly>
              <AdminLayout>
                <AddProductPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute adminOnly>
              <AdminLayout>
                <UsersPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/transactions"
          element={
            <ProtectedRoute adminOnly>
              <AdminLayout>
                <TransactionsPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/royalties"
          element={
            <ProtectedRoute adminOnly>
              <AdminLayout>
                <RoyaltiesPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/beneficiaries"
          element={
            <ProtectedRoute adminOnly>
              <AdminLayout>
                <BeneficiariesPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/audit-logs"
          element={
            <ProtectedRoute adminOnly>
              <AdminLayout>
                <AuditLogsPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />

        {/* WILDCARD 404 */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
};
