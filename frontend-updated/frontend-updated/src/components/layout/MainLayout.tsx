import React from "react";
import { Navbar } from "./Navbar";
import { Footer } from "./Footer";
import { useSelector, useDispatch } from "react-redux";
import { RootState } from "../../store";
import { setCartModalMessage, setOwnershipWarning } from "../../store/slices/uiSlice";
import { Modal } from "../ui/Modal";
import { Button } from "../ui/Button";
import { useNavigate } from "react-router-dom";

export const MainLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { ownershipWarningBookName, cartModalMessage } = useSelector(
    (state: RootState) => state.ui
  );

  return (
    <div className="min-h-screen flex flex-col bg-[#FDFCFB] dark:bg-slate-950 text-slate-900 dark:text-slate-100 transition-colors">
      <Navbar />
      <main className="flex-1">{children}</main>
      <Footer />

      {/* GLOBAL OWNERSHIP WARNING MODAL */}
      <Modal
        isOpen={!!ownershipWarningBookName}
        onClose={() => dispatch(setOwnershipWarning(null))}
        title="Already Owned"
      >
        <div className="text-center py-2 space-y-4">
          <p className="text-sm text-slate-600 dark:text-slate-300">
            You already own or have an active rental for{" "}
            <span className="font-bold text-slate-900 dark:text-white">
              "{ownershipWarningBookName}"
            </span>{" "}
            in your shelf.
          </p>
          <div className="flex gap-3 justify-center pt-2">
            <Button
              variant="outline"
              onClick={() => dispatch(setOwnershipWarning(null))}
            >
              Close
            </Button>
            <Button
              variant="gold"
              onClick={() => {
                dispatch(setOwnershipWarning(null));
                navigate("/shelf");
              }}
            >
              Go to My Shelf
            </Button>
          </div>
        </div>
      </Modal>

      {/* GLOBAL CART MESSAGE MODAL */}
      <Modal
        isOpen={!!cartModalMessage}
        onClose={() => dispatch(setCartModalMessage(null))}
        title="Notice"
      >
        <div className="text-center py-2 space-y-4">
          <p className="text-base font-semibold text-slate-800 dark:text-slate-200">
            {cartModalMessage}
          </p>
          <Button
            variant="gold"
            onClick={() => dispatch(setCartModalMessage(null))}
          >
            OK
          </Button>
        </div>
      </Modal>
    </div>
  );
};
