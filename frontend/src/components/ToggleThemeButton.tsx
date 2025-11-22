import { Button } from "antd";
import { HiOutlineMoon, HiOutlineSun } from "react-icons/hi";

interface ToggleThemeButtonProps {
    darkTheme: boolean;
    toggleTheme: () => void;
}

const ToggleThemeButton: React.FC<ToggleThemeButtonProps> = ({ darkTheme, toggleTheme }) => {
    return (
        <div className="toggle-theme-btn">
            <Button onClick={toggleTheme}>
                {darkTheme ? <HiOutlineSun /> : <HiOutlineMoon />}
            </Button>
        </div>
    );
};

export default ToggleThemeButton;
